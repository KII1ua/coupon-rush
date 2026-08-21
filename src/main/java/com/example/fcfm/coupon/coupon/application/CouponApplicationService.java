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
        Coupon coupon = coupons.findByIdForUpdate(command.couponId());

        if(!(couponCount(coupon.getId()) > 0)) {
            throw new IllegalArgumentException("남아 있는 쿠폰이 없습니다.");
        }
        return issueCoupons.save(command.couponId(), command.userId());
    }

    // 남아 있는 쿠폰 개수
    @Transactional
    public Integer couponCount(Long couponId) {
        Coupon coupon = coupons.findById(couponId);

        int totalQuantity = coupon.getTotalQuantity();

        int issueCount = issueCoupons.countByCouponId(couponId);

        return totalQuantity - issueCount;
    }

    @Transactional
    public void deleteStock(Long couponId) {
        issueCoupons.deleteAllByCouponId(couponId);
    }
}
