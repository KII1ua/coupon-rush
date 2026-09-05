package com.example.fcfm.coupon.coupon.domain;

public interface Coupons {
    Coupon save(int quantity);
    Coupon findById(Long couponId);
    Coupon findByIdForUpdate(Long couponId);
    boolean decreaseRemain(Long couponId);
    void resetRemain(Long couponId);
}
