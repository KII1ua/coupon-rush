package com.example.fcfm.coupon.coupon.domain;

import lombok.Getter;

@Getter
public class IssueCoupon {
    private final Long id;
    private Long couponId;
    private Long userId;

    private IssueCoupon(Long id, Long couponId, Long userId) {
        this.id = id;
        this.couponId = couponId;
        this.userId = userId;
    }

    public static IssueCoupon from(Long id, Long couponId, Long userId) {
        return new IssueCoupon(id, couponId, userId);
    }
}
