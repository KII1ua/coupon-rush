package com.example.fcfm.coupon.coupon.domain;

import lombok.Getter;

@Getter
public class Coupon {
    private final Long id;
    private final int totalQuantity;

    private Coupon(Long id, int totalQuantity) {
        this.id =  id;
        this.totalQuantity = totalQuantity;
    }

    public static Coupon create(int totalQuantity) {
        if(totalQuantity <= 0) throw new IllegalArgumentException("쿠폰 수량은 1개 이상이어야 합니다.");
        return new Coupon(null, totalQuantity);
    }

    public static Coupon from(Long id, int totalQuantity) {
        return new Coupon(id, totalQuantity);
    }
}
