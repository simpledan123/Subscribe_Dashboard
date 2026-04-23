# 개인 구독 관리 대시보드

여러 계정에 분산된 구독 서비스의 결제일과 잔여 기간을 한 눈에 관리하기 위해 개발한 프로젝트.
학생 무료 혜택용 깡통 계정부터 AI 서비스, 잡플래닛 같은 취준 도구까지
계정별·서비스별로 흩어진 구독을 통합 관리하고, 청구서 자동 발행과 결제 이력을 추적한다.

[![QA (pytest + playwright)](https://github.com/simpledan123/Subscribe_Dashboard/actions/workflows/qa.yml/badge.svg)](https://github.com/simpledan123/Subscribe_Dashboard/actions/workflows/qa.yml)

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Java 25, Spring Boot 4.0.1, Spring Security + JWT |
| Database | PostgreSQL, Spring Data JPA |
| Cache | Redis (INCR 원자적 카운터, 차등 TTL 캐싱) |
| Messaging | Apache Kafka (이벤트 파이프라인) |
| Frontend | React 18, Vite, Recharts |
| DevOps | Docker, GitHub Actions CI/CD, AWS RDS |
| QA | Python, pytest, Playwright (E2E 자동화) |

---

## 주요 구현 및 트러블슈팅

### 1. Redis INCR 원자적 카운터 — Race Condition 해결

**문제**
API 호출 한도 체크 시 `checkApiLimit()`과 `recordApiCall()`이 별도 트랜잭션으로 분리되어 있어, 동시 요청이 몰릴 경우 한도 초과 케이스가 중복 통과되는 race condition이 발생하였다.

**해결**
Redis INCR 단일 원자적 연산으로 전환하여 카운터 증가와 한도 체크를 분리 불가능한 단일 단계로 처리하였다. 월별 카운터 키는 `api_calls:{tenantId}:{yyyy}:{MM}` 패턴으로 관리하며 35일 TTL로 자동 만료되도록 하였다. 한도 초과 시 카운터를 즉시 원복하고 429 응답을 반환한다.

**Redis 장애 대응**
Redis 장애 시 DB fallback으로 전환하여 서비스 중단 없이 한도 체크를 이어서 처리한다. 동시성 보장은 약해지지만 가용성을 우선으로 판단하였다.

```java
try {
    currentCount = redisTemplate.opsForValue().increment(key);
} catch (Exception e) {
    return recordApiCallFallback(tenantId, now, maxApiCalls); // DB fallback
}
```

---

### 2. Redis 캐시 차등 TTL 전략

**문제**
API 호출 한도 체크 시 매 요청마다 Plan과 Tenant를 DB에서 반복 조회하여 불필요한 DB 부하가 발생하였다.

**해결**
변경 빈도에 따라 TTL을 차등 설정하였다. Plan은 TTL 1시간, Tenant는 TTL 10분, planLimits(활성 구독의 플랜 한도)는 TTL 10분으로 설정하였다. 수정·삭제 시 `@CacheEvict`로 즉시 무효화하여 데이터 정합성을 유지한다.

| 캐시 | TTL | 무효화 시점 |
|------|-----|------------|
| plans | 1시간 | 플랜 수정·삭제 |
| tenants | 10분 | 고객사 수정·삭제 |
| planLimits | 10분 | 플랜 변경·구독 취소 |

---

### 3. Kafka 이벤트 파이프라인 — 도메인 책임 분리

**문제**
구독 생성 시 청구서 생성을 동기 처리하던 구조에서 청구서 생성 실패가 구독 생성 전체를 롤백시키는 문제가 있었다.

**해결**
구독 저장 완료 후 Kafka 이벤트(`CREATED` / `CANCELLED` / `PLAN_CHANGED`)를 발행하고, Consumer가 비동기로 청구서를 생성하도록 두 도메인의 트랜잭션을 분리하였다. `tenantId`를 파티션 키로 사용하여 동일 테넌트 이벤트의 순서를 보장하였다.

```
구독 생성 API → Subscription 저장 → Kafka 이벤트 발행 → 즉시 응답
                                           ↓
                              Consumer → 청구서 비동기 생성
```

---

### 4. Spring Scheduler 청구서 자동화 + 멱등성 보장

매월 수동으로 생성하던 청구서를 Spring Scheduler로 매월 1일 자정에 전체 활성 구독 대상으로 자동 발행하도록 개선하였다.

스케줄러 재실행 또는 서버 재시작 시 같은 달 청구서가 중복 생성되는 문제를 방지하기 위해 멱등성 체크를 추가하였다. 발행 전 해당 월 청구서 존재 여부를 확인하고, 이미 존재하면 건너뛴다. 건별 예외를 격리하여 개별 실패가 전체 처리를 중단시키지 않도록 하였다.

---

### 5. N+1 문제 해결

`findAll()` 계열 API에서 Lazy Loading으로 인해 연관 엔티티마다 추가 쿼리가 발생하는 문제를 `@EntityGraph`로 해결하였다.

```java
@EntityGraph(attributePaths = {"tenant", "plan"})
List<Subscription> findAll();
```

---

### 6. JWT 인증 필터

`OncePerRequestFilter`를 구현하여 모든 요청에서 JWT 토큰을 검증하도록 하였다. 시크릿 키는 환경변수(`JWT_SECRET`)로 분리하여 코드에 하드코딩되지 않도록 하였다.

---

### 7. 전역 예외 처리

`@RestControllerAdvice`로 전역 예외 핸들러를 구성하여 모든 예외가 일관된 형식으로 응답되도록 하였다.

| 예외 | HTTP 상태 |
|------|----------|
| `ResourceNotFoundException` | 404 |
| `IllegalArgumentException` | 400 |
| `ApiLimitExceededException` | 429 |
| 그 외 `Exception` | 500 |

---

### 8. GitHub Actions E2E 자동화

Python + pytest + Playwright 기반 E2E 테스트를 작성하고 GitHub Actions CI에 통합하였다. PR마다 PostgreSQL·Redis·Kafka 컨테이너를 포함한 전체 파이프라인이 자동으로 검증된다.

**테스트 커버리지**
- 로그인 / 로그아웃
- 고객사 CRUD
- 플랜 CRUD
- 구독 생성 → 청구서 발행 → 결제 완료 플로우

---

## 아키텍처

```
┌─────────────────────────────────────────────────────┐
│                    React 18 (Vite)                  │
└─────────────────────┬───────────────────────────────┘
                      │ REST API + JWT
┌─────────────────────▼───────────────────────────────┐
│              Spring Boot 4.0.1 (Java 25)            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │  Tenant     │  │Subscription │  │   Invoice   │ │
│  │  Service    │  │  Service    │  │   Service   │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘ │
│         │                │ Kafka           │        │
│  ┌──────▼──────┐  ┌──────▼──────┐         │        │
│  │   Redis     │  │Subscription│         │        │
│  │   Cache     │  │EventConsumer│─────────┘        │
│  └─────────────┘  └─────────────┘                  │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│              PostgreSQL (AWS RDS)                   │
└─────────────────────────────────────────────────────┘
```

---

## 실행 방법

### 사전 요구사항
- Java 25
- Node.js 20 이상
- Docker

### 인프라 실행

```bash
# PostgreSQL
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=saas_subscription \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=1234 \
  postgres:16

# Redis
docker run -d -p 6379:6379 redis:7

# Kafka
docker run -d -p 9092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  apache/kafka:3.7.0
```

### Backend 실행

```bash
./gradlew bootRun
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

### Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

- 웹: http://localhost:5173

### E2E 테스트 실행

```bash
cd qa
python -m venv .venv
source .venv/bin/activate  # Windows: .\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m playwright install chromium
pytest -m e2e --html=test-results/report.html --self-contained-html
```

---

## API 명세

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/auth/register | 회원가입 |
| POST | /api/auth/login | 로그인 |
| GET | /api/tenants | 고객사 전체 조회 |
| POST | /api/tenants | 고객사 생성 |
| PUT | /api/tenants/{id} | 고객사 수정 |
| DELETE | /api/tenants/{id} | 고객사 삭제 |
| GET | /api/plans | 플랜 전체 조회 |
| POST | /api/plans | 플랜 생성 |
| PUT | /api/plans/{id} | 플랜 수정 |
| DELETE | /api/plans/{id} | 플랜 삭제 |
| GET | /api/subscriptions | 구독 전체 조회 |
| POST | /api/subscriptions | 구독 생성 |
| PUT | /api/subscriptions/{id}/change-plan | 플랜 변경 |
| PUT | /api/subscriptions/{id}/cancel | 구독 취소 |
| GET | /api/invoices | 청구서 전체 조회 |
| POST | /api/invoices | 청구서 생성 |
| PUT | /api/invoices/{id}/pay | 결제 완료 처리 |
| POST | /api/usages/tenant/{id}/api-call | API 호출 기록 |

---

## AWS RDS 운영 경험

구독 관리 서비스의 운영 환경을 가정하고 AWS RDS(PostgreSQL)로 DB 인스턴스를 직접 구성하였다.

- RDS 인스턴스 생성 및 엔드포인트 기반 연결 구성
- 보안 그룹 인바운드 규칙 최소화로 접근 제어
- SSL 연결 옵션 적용 (verify-full 방식)
- 스냅샷 기반 백업·복구 절차 정리
- 커넥션 수, CPU, 스토리지 모니터링 체크 항목 정리
- 장애 상황 우선 확인 항목(커넥션 폭증, Lock 대기, Slow query) 기준으로 대응 순서 정리
