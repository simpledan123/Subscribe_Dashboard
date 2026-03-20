# SaaS 구독 관리 플랫폼

SaaS 구독 관리 시스템입니다. 고객사 관리, 구독 플랜 설정, 사용량 트래킹, 청구서 발행 등 핵심 기능을 제공합니다.

## 주요 기능

### 대시보드
- 전체 매출, 고객사 수, 구독 현황 통계
- 월별 매출 추이 차트
- 플랜별 구독 현황 파이차트
- 최근 가입 고객 리스트

### 고객사(Tenant) 관리
- 고객사 등록, 수정, 삭제
- 고객사별 상태 관리 (ACTIVE, INACTIVE, SUSPENDED)

### 구독 플랜 관리
- 플랜 생성 (Free, Pro, Enterprise 등)
- 플랜별 가격 설정 (월간/연간)
- 플랜별 제한 설정 (API 호출 수, 저장공간, 사용자 수)

### 구독 관리
- 구독 생성 및 취소
- 플랜 변경 (업그레이드/다운그레이드)
- 결제 주기 관리 (월간/연간)
- 구독 이벤트 기반 비동기 처리 (Kafka)

### 사용량 관리
- 고객사별 API 호출 횟수 트래킹
- Redis INCR 기반 원자적 카운팅 (동시 요청 race condition 방지)
- 플랜 한도 초과 시 차단 (429 에러)
- 사용량 현황 시각화 (프로그레스 바)

### 청구서 관리
- 매월 1일 자동 청구서 생성 (Spring Scheduler)
- 결제 상태 관리 (PENDING, PAID, OVERDUE)
- 결제 완료 처리


## 기술 스택

### Backend
- Java 25
- Spring Boot 4.0.1
- Spring Security + JWT
- Spring Data JPA
- Spring Data Redis
- Spring Kafka
- PostgreSQL
- Redis
- Kafka
- Swagger UI

### Frontend
- React 18
- Vite
- React Router DOM
- Axios
- Recharts


## 주요 기술적 개선 사항

### 1. Redis Atomic Counter (race condition 해결)
API 호출 한도 체크 시 `checkApiLimit()`과 `recordApiCall()`이 별도 트랜잭션으로 분리되어
동시 요청이 몰릴 경우 한도 초과 케이스가 중복 통과되는 문제가 있었습니다.
Redis INCR 단일 원자적 연산으로 전환해 동시 요청 100건 기준 한도 정확도 100%를 보장합니다.

### 2. Redis Cache (반복 DB 조회 개선)
API 호출 한도 체크 시 매 요청마다 Plan과 Tenant를 DB에서 반복 조회하는 문제를 확인했습니다.
변경 빈도가 낮은 Plan은 TTL 1시간, 상태 변경이 발생할 수 있는 Tenant는 TTL 10분으로 차등 설정해
Redis 캐시를 적용했습니다. 수정/삭제 시 `@CacheEvict`로 즉시 무효화해 데이터 정합성을 유지합니다.

### 3. Kafka 이벤트 파이프라인 (도메인 책임 분리)
구독 생성 시 청구서 생성을 동기 처리하던 구조에서, 청구서 생성 실패가 구독 생성 전체를
롤백시키는 문제가 있었습니다. Kafka 이벤트 파이프라인으로 분리해 두 도메인의 트랜잭션을
독립시켰으며, 파티션 키로 tenantId를 사용해 동일 테넌트 이벤트의 순서를 보장합니다.

### 4. Spring Scheduler 기반 월별 청구서 자동화
매월 수동으로 청구서를 생성하던 방식에서, Spring Scheduler를 활용해 매월 1일 자정에
전체 활성 구독에 대한 청구서를 자동 발행하도록 개선했습니다. 개별 구독의 청구서 생성 실패가
전체 처리를 중단시키지 않도록 예외를 건별로 격리해 안정성을 확보했습니다.


## 실행 방법

### 사전 요구사항
- Java 17 이상
- Node.js 18 이상
- PostgreSQL
- Redis
- Kafka

### 인프라 실행 (Docker)
```bash
# Redis
docker run -d -p 6379:6379 --name redis redis

# Kafka
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_CFG_PROCESS_ROLES=broker,controller \
  -e KAFKA_CFG_NODE_ID=1 \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  apache/kafka:3.7.0
```

### 데이터베이스 설정
```sql
CREATE DATABASE saas_subscription;
```

### Backend 실행
```bash
gradlew bootRun
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

## QA Automation (Python + pytest + Playwright)

- E2E tests run on every PR via GitHub Actions
- Covers: Login, Navigation/Logout, Tenant CRUD, Plan CRUD, Subscription → Invoice → Pay flow

### CI Badge
[![QA (pytest + playwright)](https://github.com/simpledan123/Subscribe_Dashboard/actions/workflows/qa.yml/badge.svg)](https://github.com/simpledan123/Subscribe_Dashboard/actions/workflows/qa.yml)

### Run locally
```bash
# Terminal 1 (backend)
./gradlew bootRun

# Terminal 2 (frontend)
cd frontend
npm install
npm run dev

# Terminal 3 (qa)
cd qa
python -m venv .venv
source .venv/bin/activate  # Windows: .\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m playwright install chromium
pytest -m e2e --html=test-results/report.html --self-contained-html
```

## AWS RDS (PostgreSQL) 실습/운영 경험

구독 관리 서비스의 운영 환경을 가정하고 AWS RDS(PostgreSQL)로 DB 인스턴스를 구성했습니다.
로컬(PostgreSQL)과 동일한 스키마/쿼리를 클라우드 환경에서도 재현할 수 있도록 연결/운영 포인트를 정리했습니다.

### 1) 인스턴스 구축 및 연결
- RDS PostgreSQL 인스턴스 생성 및 엔드포인트 기반 연결 구성
- 보안 그룹 인바운드 규칙을 최소화하여 접근 제어(허용 IP/포트 제한)
- SSL 연결 옵션을 적용하여 안전한 접속 설정(verify-full 방식)

### 2) 백업/복구 시나리오 정리
- 스냅샷(수동 백업) 기반 복구 절차를 정리하여 장애/실수 상황에 대한 대응 흐름을 마련
- 복구 시 신규 인스턴스 생성 → 애플리케이션 재연결 순으로 복구 전략을 구성

### 3) 운영 관점 점검(모니터링/장애 포인트)
- 커넥션 수, CPU, 스토리지 등 주요 지표를 기준으로 상태를 점검할 수 있도록 체크 항목을 정리
- 장애 상황에서 우선 확인할 항목(커넥션 폭증, Lock 대기, Slow query)을 기준으로 대응 순서를 정리

## API 명세

### 인증
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/auth/register | 회원가입 |
| POST | /api/auth/login | 로그인 |

### 고객사
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/tenants | 전체 조회 |
| GET | /api/tenants/{id} | 단일 조회 |
| POST | /api/tenants | 생성 |
| PUT | /api/tenants/{id} | 수정 |
| DELETE | /api/tenants/{id} | 삭제 |

### 플랜
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/plans | 전체 조회 |
| GET | /api/plans/{id} | 단일 조회 |
| POST | /api/plans | 생성 |
| PUT | /api/plans/{id} | 수정 |
| DELETE | /api/plans/{id} | 삭제 |

### 구독
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/subscriptions | 전체 조회 |
| POST | /api/subscriptions | 생성 |
| PUT | /api/subscriptions/{id}/change-plan | 플랜 변경 |
| PUT | /api/subscriptions/{id}/cancel | 구독 취소 |

### 사용량
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/usages | 전체 조회 |
| POST | /api/usages/tenant/{id}/api-call | API 호출 기록 |

### 청구서
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/invoices | 전체 조회 |
| POST | /api/invoices | 생성 |
| PUT | /api/invoices/{id}/pay | 결제 완료 |
