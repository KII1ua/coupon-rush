package com.example.fcfm.coupon.coupon.domain;

public interface CouponIssuer {
    void open(Long couponId, int totalQuantity);
    IssueResult tryIssue(Long couponId, Long userId);
    void cancel(Long couponId, Long userId);
    int issuedCount(Long couponId);
}
