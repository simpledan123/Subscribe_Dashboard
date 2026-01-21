# SaaS 구독 관리 플랫폼

B2B SaaS  구독 관리 시스템입니다. 고객사 관리, 구독 플랜 설정, 사용량 트래킹, 청구서 발행 등 핵심 기능을 제공합니다.

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

### 사용량 관리
- 고객사별 API 호출 횟수 트래킹
- 플랜 한도 초과 시 차단 (429 에러)
- 사용량 현황 시각화 (프로그레스 바)

### 청구서 관리
- 청구서 자동 생성
- 결제 상태 관리 (PENDING, PAID, OVERDUE)
- 결제 완료 처리

### 인증
- JWT 기반 관리자 인증
- 회원가입 / 로그인 / 로그아웃

## 기술 스택

### Backend
- Java 25
- Spring Boot 4.0.1
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Swagger UI

### Frontend
- React 18
- Vite
- React Router DOM
- Axios
- Recharts

## 프로젝트 구조
```
subscription-platform/
├── src/main/java/com/saas/subscriptionplatform/
│   ├── config/          # CORS, Security 설정
│   ├── controller/      # REST API 컨트롤러
│   ├── entity/          # JPA 엔티티
│   ├── repository/      # 데이터 접근 계층
│   ├── security/        # JWT 유틸
│   └── service/         # 비즈니스 로직
├── src/main/resources/
│   └── application.yml
├── frontend/
│   ├── src/
│   │   ├── pages/
│   │   ├── App.jsx
│   │   └── App.css
│   └── package.json
└── build.gradle
```

## 실행 방법

### 사전 요구사항
- Java 17 이상
- Node.js 18 이상
- PostgreSQL

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

