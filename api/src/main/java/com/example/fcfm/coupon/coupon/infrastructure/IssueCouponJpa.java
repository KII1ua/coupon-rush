package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.IssueCoupon;
import com.example.fcfm.coupon.coupon.domain.IssueCoupons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class IssueCouponJpa implements IssueCoupons{

    private final IssueCouponJpaRepository delegate;

    @Override
    public IssueCoupon save(Long couponId, Long userId) {
        IssueCouponEntity entity = delegate.save(IssueCouponEntity.from(userId, couponId));

        return entity.toDomain();
    }

    @Override
    public Integer countByCouponId(Long couponId) {
        return delegate.countByCouponId(couponId);
    }

    @Override
    public void deleteAllByCouponId(Long couponId) {
        delegate.deleteAllByCouponId(couponId);
    }
}
