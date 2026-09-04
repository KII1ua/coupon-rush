package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.IssueCoupons;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponIssuedConsumer {

    private final IssueCoupons issueCoupons;

    @KafkaListener(topics = KafkaCouponIssuedPublisher.TOPIC)
    public void handle(CouponIssuedMessage message) {
        try {
            issueCoupons.save(message.couponId(), message.userId());
        } catch (DataIntegrityViolationException e) {
            // 같은 메시지가 두 번 소비된 경우(at-least-once). 유니크 제약이 걸러주므로 무시 = 멱등 처리
            log.warn("중복 발급 메시지 무시: couponId={}, userId={}", message.couponId(), message.userId());
        }
    }
}
