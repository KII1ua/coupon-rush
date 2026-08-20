import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// 사용법:
//   k6 run -e COUPON_ID=2 -e VUS=1000 k6/issue-test.js
// VUS명이 "동시에" 각 1번씩 발급 요청을 보낸다 (선착순 레이스 재현)

const COUPON_ID = __ENV.COUPON_ID || '1';
const BASE_URL  = __ENV.BASE_URL  || 'http://localhost:8080';
const VUS       = parseInt(__ENV.VUS || '1000');

export const options = {
  scenarios: {
    burst: {
      executor: 'per-vu-iterations',
      vus: VUS,
      iterations: 1,        // VU당 1회 = 서로 다른 유저 VUS명이 동시 요청
      maxDuration: '2m',
    },
  },
};

const issued   = new Counter('coupon_issued');
const soldOut  = new Counter('coupon_sold_out');
const errors   = new Counter('coupon_errors');

export default function () {
  const userId = __VU; // VU 번호를 userId로 사용 → 전원 서로 다른 유저

  const res = http.post(
    `${BASE_URL}/api/coupons/${COUPON_ID}/issue`,
    JSON.stringify({ userId }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  if (res.status === 200) {
    issued.add(1);
  } else if (res.status === 500 || res.status === 400 || res.status === 409) {
    soldOut.add(1);
  } else {
    errors.add(1);
  }

  check(res, { 'status 200 or expected fail': (r) => r.status === 200 || r.status === 500 });
}
