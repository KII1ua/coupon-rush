package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.Coupon;
import com.example.fcfm.coupon.coupon.domain.Coupons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponJpa implements Coupons {
    private final CouponJpaRepository delegate;

    @Override
    public Coupon save(int quantity) {
        Coupon coupon = Coupon.create(quantity);

        CouponEntity entity = CouponEntity.from(coupon);
        delegate.save(entity);
        return entity.toDomain();
    }

    @Override
    public Coupon findById(Long couponId) {
       return delegate.findById(couponId)
                .map(CouponEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
    }

    @Override
    public Coupon findByIdForUpdate(Long couponId) {
        return delegate.findByIdForUpdate(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다.")).toDomain();
    }

    @Override
    public boolean decreaseRemain(Long couponId) {
        return delegate.decreaseRemain(couponId) == 1;
    }

    @Override
    public void resetRemain(Long couponId) {
        delegate.resetRemain(couponId);
    }
}
