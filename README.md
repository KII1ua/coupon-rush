# coupon-rush — 선착순 쿠폰 발급 시스템

대규모 트래픽이 몰리는 선착순 쿠폰 발급을 **정합성을 지키면서 얼마나 빠르게 처리할 수 있는가**를 주제로,
동시성 제어 방식을 단계적으로 바꿔가며 **같은 부하 테스트로 측정하고 병목을 추적**한 학습 프로젝트입니다.

- 요구사항: 도착 순서대로 발급 / 정해진 수량 초과 금지 / 유저당 1장 / 급증 트래픽 대응
- 모든 단계는 k6 부하 테스트로 검증했고, 다음 단계로 넘어간 이유는 항상 이전 단계의 측정값이 만들어 주었습니다

> 상세한 과정은 블로그에 정리했습니다 → [선착순 쿠폰 발급 시스템 구현](https://mari-laveau.tistory.com/308)

## 기술 스택

Java 25 · Spring Boot 4.1 · PostgreSQL 16 · Redis 7 (Lua Script) · Apache Kafka 3.8 (KRaft) · k6 · Docker Compose

## 최종 아키텍처

```
클라이언트
   │  POST /api/coupons/{id}/issue
   ▼
┌─ api 모듈 (검증 서버) ──────────────────────┐
│  Redis Lua 스크립트로 원자 판정               │
│   · 쿠폰 존재? → 중복 발급? → 재고 있음?       │
│   · 통과 시 발급자 Set에 등록                 │──(거절)──▶ 즉시 거절 응답 (DB 접근 없음)
│  당첨 시 Kafka 발행 후 즉시 응답              │
└────────────────┬───────────────────────────┘
                 │  coupon-issued 토픽 (파티션 3, 키=userId)
                 ▼
┌─ consumer 모듈 (발급 서버) ─────────────────┐
│  메시지 소비 → DB INSERT (지연 쓰기)          │
│  중복 메시지는 유니크 제약으로 멱등 처리        │
└────────────────────────────────────────────┘

역할 분담 · Redis = 당첨 판정의 원장 / Kafka = 유량 제어 버퍼 / DB = 영구 기록
```

## 개선 여정

### 0. 문제 발견 — 동시성 제어 없는 초기 구현

`재고 확인 → 발급 저장`을 애플리케이션 코드에서 순차 실행. 브라우저 기반 테스트에서는 문제가 없어 보였지만,
k6로 진짜 동시 요청을 만들자 **재고 5,000개에 5,008건이 발급**되는 초과 발급이 재현되었습니다.
확인과 저장 사이의 틈에 다른 요청이 끼어드는 전형적인 check-then-act 레이스였습니다.

> 브라우저는 호스트당 커넥션 6개로 요청을 직렬화하기 때문에 동시성 버그를 재현하지 못합니다. k6를 도입한 이유입니다.

### 1. DB 비관적 락 — `feature/permissitic-lock-v1`

쿠폰 행을 `SELECT ... FOR UPDATE`(`@Lock(PESSIMISTIC_WRITE)`)로 잠가 확인~저장을 직렬화.

- 결과: 정합성 확보 ✅ / 처리량 **1,443 req/s**로 급락, 평균 응답 137ms
- 원인: 모든 요청(품절 거절 포함)이 쿠폰 행 락 앞에 한 줄로 서고, 락을 쥔 채 count 쿼리까지 실행해 임계 구역이 김
- 관측: 톰캣 스레드 200개 중 188개가 HikariCP 커넥션 대기 (`hikaricp_connections_pending`)

### 2. 원자적 조건부 UPDATE — `feature/permissitic-lock-v2`

발급 수를 세는 방식 대신 남은 수량 컬럼을 두고, 확인과 차감을 SQL 한 문장으로 합침.

```sql
UPDATE coupon SET remain_quantity = remain_quantity - 1
WHERE id = ? AND remain_quantity > 0   -- 영향받은 행 수 1이면 발급, 0이면 품절
```

- 결과: 정합성 유지 ✅ / **5,337 req/s** (비관적 락 대비 3.7배)
- 이유: 락을 쥐는 시간이 UPDATE 한 문장으로 줄었고, 품절 이후에는 조건이 거짓이라 락 대기 없이 즉시 거절
- 한계 실험: 커넥션 풀을 10 → 50으로 늘려도 처리량은 +7%뿐. 커넥션당 점유 시간이 2ms → 8ms로 늘며 병목이 풀에서 DB CPU로 이동함을 확인 — **풀 크기는 병목을 옮길 뿐 없애지 못한다**

### 3. Redis + Lua Script — `feature/permissitic-lock-v3`

판정 자체를 DB 밖으로. 쿠폰별로 `limit`(String)과 발급자 명단 `issued`(Set)를 Redis에 두고,
`중복 확인(SISMEMBER) → 재고 확인(SCARD) → 등록(SADD)`을 Lua 스크립트 하나로 묶어 원자 실행.

- 결과: **9,087 req/s** (2,000 VU), 거절 요청이 DB에 아예 닿지 않음 (`hikaricp_connections_pending` ≈ 0)
- Redis는 단일 스레드로 스크립트를 통째로 실행하므로 락 없이도 판정이 원자적
- 남은 문제: 당첨자의 DB INSERT가 여전히 응답 경로에 있고, Redis 다운 시 판정 원장이 유실됨

### 4. Kafka 지연 쓰기 + 검증/발급 서버 분리 — `feature/v4` (최종)

당첨자의 DB 저장을 Kafka로 넘겨 응답 경로에서 제거하고, Gradle 멀티 모듈로 두 애플리케이션을 분리.

- **api (검증 서버)**: Redis 판정 → `coupon-issued` 토픽 발행(브로커 ack 대기) → 즉시 응답. 발행 실패 시 Redis 발급 취소(보상)
- **consumer (발급 서버)**: 웹서버 없는 컨슈머 전용 앱. 메시지를 DB가 감당하는 속도로 소비해 INSERT. at-least-once 중복은 `(coupon_id, user_id)` 유니크 제약으로 멱등 처리
- 파티션 키를 couponId가 아닌 **userId**로 → 인기 쿠폰 메시지가 파티션 3개에 분산되어 병렬 소비 가능

검증한 것:

- 정합성: 2,000 VU에서 발급 정확히 5,000건, Redis SCARD = DB count, 컨슈머는 종료 1초 내 따라잡음
- **장애 격리**: 컨슈머를 강제 종료한 상태에서도 발급 접수는 즉시 응답으로 계속됨 → 메시지는 브로커에 대기 → 컨슈머 재시작 시 밀린 메시지 전부 처리
- 정직한 발견: 로컬에서는 DB INSERT가 2~5ms라 처리량 개선은 미미(8,535 req/s). **지연 쓰기의 이득은 DB 쓰기 비용에 비례**하며, 이 단계의 실질 가치는 처리량이 아니라 유량 제어와 장애 격리

## 성능 측정 결과

동일 시나리오(재고 5,000 / 전원 서로 다른 유저 / 로컬 MacBook, k6 동일 머신):

| 단계 | 부하 | 발급 정합성 | 처리량 | 평균 응답 |
|---|---|---|---|---|
| 동시성 제어 없음 | 200 VU | ❌ 5,000개에 5,006~5,008건 | 4,465 req/s | 44ms |
| 비관적 락 | 200 VU | ✅ 정확히 5,000 | 1,443 req/s | 137ms |
| 원자적 UPDATE | 200 VU | ✅ | 5,337 req/s | 37ms |
| 원자적 UPDATE | 2,000 VU | ✅ | 4,744 req/s | 410ms |
| Redis + Lua | 2,000 VU | ✅ | 9,087 req/s | 206ms |
| Redis + Kafka (최종) | 2,000 VU | ✅ | 8,535 req/s | 101ms |

병목의 이동: DB 행 락 → HikariCP 커넥션 풀 경계 → DB CPU → (판정 분리 후) 애플리케이션/머신 CPU.
각 단계에서 Prometheus + Grafana로 `tomcat_threads_busy`, `hikaricp_connections_pending`, 커넥션 점유 시간을 관측해 확인했습니다.

## 실행 방법

```bash
# 1. 인프라 (PostgreSQL, Redis, Kafka)
docker compose up -d

# 2. 두 애플리케이션 (각각 별도 터미널)
./gradlew :api:bootRun
./gradlew :consumer:bootRun

# 3. 쿠폰 생성 (coupon.http 파일로도 가능)
curl -X POST http://localhost:8080/api/coupons/save/coupon \
  -H "Content-Type: application/json" -d '{"quantity": 5000}'

# 4. 부하 테스트
k6 run --vus 2000 --duration 10s -e COUPON_ID=1 k6/script.js
```

- 간단한 동작 확인용 웹 페이지: `http://localhost:8080/load-test.html`
- Redis 상태 확인: `docker exec coupon-redis redis-cli SCARD coupon:1:issued`

## 프로젝트 구조

```
coupon-rush/
├── api/               검증 서버 — REST API, Redis Lua 판정, Kafka 발행
├── consumer/          발급 서버 — Kafka 소비, DB 저장 (웹서버 없음)
├── k6/                부하 테스트 스크립트 (지속 부하형 / 버스트형)
├── coupon.http        IntelliJ HTTP Client 요청 모음
└── docker-compose.yml PostgreSQL + Redis + Kafka(KRaft)
```

두 모듈은 클래스를 공유하지 않고 메시지의 JSON 필드만을 계약으로 삼습니다 (서비스 간 결합 최소화).

## 한계와 다음 단계

- **유령 쿠폰**: Redis 등록과 Kafka 발행 사이에 서버가 죽으면 보상이 실행되지 못함 → RESERVED/ISSUED 상태 분리 + 미처리 항목 재발행 스케줄러
- **Redis 유실 대비**: 판정 원장이 Redis에만 있으므로, 다운 시 DB 기반 재구성 + 재구성 중 신규 유입 차단(드레인) 절차 필요
- 발급 순서 기록: Set → ZSet(score = 발급 순번)으로 바꾸면 "n번째 발급" 응답과 대기열 확장이 가능
- 품절 응답을 500 → 409로 정리, 컨슈머 스케일 아웃(파티션 수만큼) 실험
