package com.example.fcfm.couponconsumer;

// api 모듈의 CouponIssuedMessage와 필드가 같아야 한다 (토픽으로 주고받는 계약)
public record CouponIssuedMessage(Long couponId, Long userId) {
}
