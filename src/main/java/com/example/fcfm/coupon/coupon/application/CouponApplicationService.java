package com.example.fcfm.coupon.coupon.application;

import com.example.fcfm.coupon.coupon.application.dto.CouponCommand;
import com.example.fcfm.coupon.coupon.application.dto.IssueCouponCommand;
import com.example.fcfm.coupon.coupon.domain.Coupon;
import com.example.fcfm.coupon.coupon.domain.Coupons;
import com.example.fcfm.coupon.coupon.domain.IssueCoupon;
import com.example.fcfm.coupon.coupon.domain.IssueCoupons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponApplicationService {
    private final Coupons coupons;
    private final IssueCoupons issueCoupons;

    @Transactional
    public Coupon save(CouponCommand command) {
        return coupons.save(command.totalQuantity());
    }

    @Transactional
    public IssueCoupon saveIssueCoupon(IssueCouponCommand command) {
        if (!coupons.decreaseRemain(command.couponId())) {
            throw new IllegalArgumentException("남아 있는 쿠폰이 없습니다.");
        }
        return issueCoupons.save(command.couponId(), command.userId());
    }

    // 남아 있는 쿠폰 개수
    @Transactional(readOnly = true)
    public Integer couponCount(Long couponId) {
        return coupons.findById(couponId).getRemainQuantity();
    }

    @Transactional
    public void deleteStock(Long couponId) {
        issueCoupons.deleteAllByCouponId(couponId);
        coupons.resetRemain(couponId);
    }
}
