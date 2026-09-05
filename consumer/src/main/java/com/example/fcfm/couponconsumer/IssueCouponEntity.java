package com.example.fcfm.couponconsumer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// api 모듈과 같은 issue_coupon 테이블에 쓴다. 스키마는 api가 만들고, 컨슈머는 INSERT만 한다.
@Entity
@Getter
@Table(name = "issue_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueCouponEntity {
    @Id @GeneratedValue
    private Long id;

    private Long userId;

    private Long couponId;

    public IssueCouponEntity(Long userId, Long couponId) {
        this.userId = userId;
        this.couponId = couponId;
    }
}
