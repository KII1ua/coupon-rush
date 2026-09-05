package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.IssueCoupon;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@Table(name = "issue_coupon", uniqueConstraints = @UniqueConstraint(columnNames = {"couponId", "userId"}))
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueCouponEntity {
    @Id @GeneratedValue
    private Long id;

    private Long userId;

    private Long couponId;

    private IssueCouponEntity(Long userId, Long couponId) {
        this.userId = userId;
        this.couponId = couponId;
    }

    public static IssueCouponEntity from(Long userId, Long couponId) {
        return new IssueCouponEntity(userId, couponId);
    }

    public IssueCoupon toDomain() {
        return IssueCoupon.from(id, userId, couponId);
    }
}
