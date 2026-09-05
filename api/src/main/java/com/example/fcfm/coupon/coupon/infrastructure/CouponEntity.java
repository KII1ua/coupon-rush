package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.Coupon;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEntity {
    @Id @GeneratedValue
    private Long id;

    private Integer totalQuantity;

    private Integer remainQuantity;

    private CouponEntity(Long id, Integer totalQuantity, Integer remainQuantity) {
        this.id = id;
        this.totalQuantity = totalQuantity;
        this.remainQuantity = remainQuantity;
    }

    public static CouponEntity from(Coupon coupon) {
        return new CouponEntity(coupon.getId(), coupon.getTotalQuantity(), coupon.getRemainQuantity());
    }

    public Coupon toDomain() {
        return Coupon.from(id, totalQuantity, remainQuantity);
    }
}
