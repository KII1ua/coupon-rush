package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.Coupon;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEntity {
    @Id @GeneratedValue
    private Long id;

    private Integer totalQuantity;

    private CouponEntity(Long id, Integer totalQuantity) {
        this.id = id;
        this.totalQuantity = totalQuantity;
    }

    public static CouponEntity from(Coupon coupon) {
        return new CouponEntity(coupon.getId(), coupon.getTotalQuantity());
    }

    public Coupon toDomain() {
        return Coupon.from(id, totalQuantity);
    }
}
