package com.example.fcfm.coupon.coupon.domain;

public interface Coupons {
    Coupon save(int quantity);
    Coupon findById(Long couponId);
}
