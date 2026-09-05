package com.example.fcfm.coupon.coupon.domain;

public interface CouponIssuedPublisher {
    void publish(Long couponId, Long userId);
}
