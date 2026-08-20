import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// ── 사용법 ─────────────────────────────────────────────────────
//   k6 run --vus 200 --duration 10s k6/script.js
//   k6 run --vus 200 --duration 10s -e COUPON_ID=2 k6/script.js
//
//   COUPON_ID : 발급 대상 쿠폰 ID   (기본 1)
//   BASE_URL  : 서버 주소           (기본 http://localhost:8080)
//
// VU(가상 유저)들이 duration 동안 쉬지 않고 발급 요청을 반복한다.
// 매 요청 userId가 전부 다르므로 "서로 다른 N명의 선착순 경쟁" 시나리오.
// 테스트 후 DB 발급 건수와 쿠폰 재고를 비교해 초과 발급 여부를 확인할 것.
// ──────────────────────────────────────────────────────────────

const COUPON_ID = __ENV.COUPON_ID || '1';
const BASE_URL  = __ENV.BASE_URL  || 'http://localhost:8080';

const issued  = new Counter('coupon_issued');    // 200 → 발급 성공
const rejected = new Counter('coupon_rejected'); // 4xx/500 → 품절·중복 등 거절
const errors  = new Counter('coupon_errors');    // 그 외 (타임아웃, 커넥션 오류 등)

export default function () {
  // VU 번호 + 반복 번호 조합으로 전 요청에서 유일한 userId 생성
  const userId = __VU * 1_000_000 + __ITER;

  const res = http.post(
    `${BASE_URL}/api/coupons/${COUPON_ID}/issue`,
    JSON.stringify({ userId }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  if (res.status === 200) issued.add(1);
  else if (res.status >= 400 && res.status <= 500) rejected.add(1);
  else errors.add(1);

  check(res, {
    '발급 성공 또는 정상 거절': (r) => r.status === 200 || (r.status >= 400 && r.status <= 500),
  });
}

// 테스트 종료 후 남은 재고를 한 번 조회해서 요약에 같이 출력
export function teardown() {
  const res = http.get(`${BASE_URL}/api/coupons/${COUPON_ID}/stock`);
  if (res.status === 200) {
    console.log(`[teardown] 쿠폰 ${COUPON_ID} 서버 기준 남은 재고 응답: ${res.body}`);
  }
}
