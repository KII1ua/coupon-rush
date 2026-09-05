package com.example.fcfm.coupon.coupon.application;

import com.example.fcfm.coupon.coupon.application.dto.CouponCommand;
import com.example.fcfm.coupon.coupon.application.dto.IssueCouponCommand;
import com.example.fcfm.coupon.coupon.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponApplicationService {
    private final Coupons coupons;
    private final IssueCoupons issueCoupons;
    private final CouponIssuer issuer;
    private final CouponIssuedPublisher publisher;

    @Transactional
    public Coupon save(CouponCommand command) {
        Coupon coupon = coupons.save(command.totalQuantity());
        issuer.open(coupon.getId(), coupon.getTotalQuantity());
        return coupon;
    }

    // 트랜잭션 사용 X. Redis 판정 → Kafka 발행까지가 요청 경로이고, DB 저장은 컨슈머가 뒤에서 처리한다.
    public void issue(IssueCouponCommand command) {
        IssueResult result = issuer.tryIssue(command.couponId(), command.userId());

        switch (result) {
            case SOLD_OUT -> throw new IllegalArgumentException("남아 있는 쿠폰이 없습니다.");
            case DUPLICATED -> throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
            case NOT_FOUND -> throw new IllegalArgumentException("존재하지 않는 쿠폰입니다.");
            case ISSUED -> { }
        }

        try {
            publisher.publish(command.couponId(), command.userId());
        } catch (RuntimeException e) {
            issuer.cancel(command.couponId(), command.userId());
            throw e;
        }
    }

    // 남아 있는 쿠폰 개수
    @Transactional(readOnly = true)
    public Integer couponCount(Long couponId) {
        Coupon coupon = coupons.findById(couponId);
        return coupon.getTotalQuantity() - issuer.issuedCount(couponId);
    }

    @Transactional
    public void deleteStock(Long couponId) {
        Coupon coupon = coupons.findById(couponId);
        issueCoupons.deleteAllByCouponId(couponId);
        issuer.open(couponId, coupon.getTotalQuantity());
    }
}
