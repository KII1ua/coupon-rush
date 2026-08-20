package com.example.fcfm.coupon.coupon.domain;

import java.util.List;

public interface IssueCoupons {
    IssueCoupon save(Long couponId, Long userId);

    Integer countByCouponId(Long couponId);

    void deleteAllByCouponId(Long couponId);
}
