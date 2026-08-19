package com.example.fcfm.coupon.user.domain;

import lombok.Getter;

@Getter
public class User {
    private final Long id;
    private final String name;
    private Integer coupons;

    private User(Long id, String name, Integer coupons) {
        this.id = id;
        this.name = name;
        this.coupons = coupons;
    }

    public static User from(Long id, String name, Integer coupons) {
        return new User(id, name, coupons);
    }

    public Integer addCoupon(Integer couponCount) {
        if(couponCount <= 0) throw new IllegalArgumentException("쿠폰이 존재하지 않습니다.");
        this.coupons += 1;
        return --couponCount;
    }
}
