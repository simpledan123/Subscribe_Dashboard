# Subtrack — 개인 구독 관리 대시보드

여러 이메일 계정에 흩어진 개인 구독을 한곳에서 관리하는 풀스택 프로젝트입니다. 유료 구독뿐 아니라 학생 인증, 무료 체험, 가족 공유처럼 **돈은 결제되지 않지만 종료일을 놓치면 안 되는 혜택**도 같은 구독 모델로 관리합니다.

[![QA](https://github.com/simpledan123/Subscribe_Dashboard/actions/workflows/qa.yml/badge.svg)](https://github.com/simpledan123/Subscribe_Dashboard/actions/workflows/qa.yml)

## 해결하려는 문제

- ChatGPT, Claude 등 AI 서비스가 여러 이메일 계정에 분산됨
- 학생용 계정의 GitHub·Notion 무료 혜택 종료일을 기억하기 어려움
- 잡플래닛 같은 취업 준비 서비스의 자동 갱신일을 놓치기 쉬움
- 월간·연간 구독이 섞여 실제 월 예상 지출을 파악하기 어려움

## 주요 기능

| 기능 | 내용 |
|---|---|
| 계정 관리 | 서비스 로그인 이메일, 별칭, 사용 목적 분류 |
| 서비스 관리 | 외부 서비스와 플랜, 월·연 가격, 사용 한도 관리 |
| 구독 관리 | 결제 주기, 실제 가격, 자동 갱신, 다음 결제일, 혜택 종료일 관리 |
| 결제 내역 | 예정 결제와 완료된 실제 결제 기록 분리 |
| 혜택·사용량 | AI 메시지·크레딧·저장공간 및 학생 혜택 만료일 추적 |
| 대시보드 | 월 환산 예상 지출, 예정 결제, 카테고리별 지출 집계 |

## 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Java 25, Spring Boot 4, Spring Security, JWT, Spring Data JPA |
| Database | PostgreSQL |
| Cache | Redis (서비스/계정 캐시, 사용량 원자적 누적) |
| Messaging | Apache Kafka (구독 이벤트 → 결제 일정 비동기 생성) |
| Frontend | React 19, Vite, Recharts, Axios |
| QA | pytest, Playwright, GitHub Actions |

## 도메인 구조

```text
ServiceAccount ─┐
                ├─ Subscription ── PaymentRecord
ServicePlan ────┘          └────── BenefitUsage
```

- `ServiceAccount`: 외부 서비스에 로그인하는 개인 이메일 계정
- `ServicePlan`: ChatGPT Plus, GitHub Student Pack 같은 서비스·플랜 정보
- `Subscription`: 계정과 플랜을 연결하며 결제일과 혜택 종료일을 관리
- `PaymentRecord`: 청구서가 아니라 개인의 결제 예정·완료 기록
- `BenefitUsage`: 메시지, 크레딧, GB 등 서비스별 선택적 사용량

## 비동기·캐시 설계

구독 생성 API는 구독 저장 후 Kafka 이벤트를 발행하고 바로 응답합니다. Consumer는 다음 결제 일정을 비동기로 생성합니다. `subscription_id + scheduled_date` 복합 유니크 제약과 존재 여부 확인으로 이벤트 재처리나 스케줄러 재실행 시 중복 일정을 방지합니다.

서비스 플랜은 Redis에 1시간, 서비스 계정은 10분 캐시합니다. 혜택 사용량 증가 API는 Redis `INCR`를 이용하며 Redis 장애 시 DB 누적으로 전환합니다.

매일 자정 이후 스케줄러가 30일 이내 활성 구독을 조회해 빠진 결제 일정을 보완합니다. 무료 구독은 결제 일정 대상에서 제외됩니다.

## 실행 방법

### Docker Compose로 전체 실행

```bash
docker compose up --build
```

- 웹: http://localhost:5173
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- 데모 로그인: `demo` / `demo1234`

Compose는 `demo` 프로필을 사용하므로 계정·서비스·구독·결제·혜택 샘플 데이터가 빈 DB에 자동 생성됩니다.

### 로컬 개발

```bash
docker compose up -d postgres redis kafka
SPRING_PROFILES_ACTIVE=demo ./gradlew bootRun
```

```bash
cd frontend
npm ci
npm run dev
```

Node.js 20 이상과 Java 25가 필요합니다. 프런트 API 주소를 바꾸려면 `VITE_API_URL` 환경 변수를 사용합니다.

> 기존 기업용 버전의 DB 스키마와 호환되지 않습니다. 기존 `postgres_data` 볼륨을 재사용한다면 별도 백업 후 새 DB/볼륨으로 시작하는 것을 권장합니다.

## API

모든 도메인 API는 JWT 인증이 필요합니다.

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/auth/register` | 사용자 가입 |
| POST | `/api/auth/login` | JWT 발급 |
| GET/POST | `/api/accounts` | 서비스 계정 조회·등록 |
| PUT/DELETE | `/api/accounts/{id}` | 서비스 계정 수정·삭제 |
| GET/POST | `/api/services` | 서비스 플랜 조회·등록 |
| PUT/DELETE | `/api/services/{id}` | 서비스 플랜 수정·삭제 |
| GET/POST | `/api/subscriptions` | 구독 조회·등록 |
| PUT | `/api/subscriptions/{id}` | 구독 정보 수정 |
| PUT | `/api/subscriptions/{id}/cancel` | 자동 갱신 해제 및 취소 |
| GET/POST | `/api/payments` | 결제 기록 조회·일정 생성 |
| PUT | `/api/payments/{id}/paid` | 결제 완료 처리 |
| GET/POST | `/api/benefits` | 혜택·사용량 조회·저장 |
| POST | `/api/benefits/{subscriptionId}/add` | 사용량 원자적 누적 |

## 검증

```bash
./gradlew test
cd frontend && npm run lint && npm run build
```

전체 E2E는 PostgreSQL·Redis·Kafka와 양쪽 애플리케이션을 실행한 뒤 아래처럼 수행합니다.

```bash
cd qa
pip install -r requirements.txt
python -m playwright install chromium
pytest -m e2e
```
