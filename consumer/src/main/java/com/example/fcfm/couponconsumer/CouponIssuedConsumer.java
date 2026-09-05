package com.example.fcfm.couponconsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponIssuedConsumer {

    private final IssueCouponJpaRepository repository;

    @KafkaListener(topics = "coupon-issued")
    public void handle(CouponIssuedMessage message) {
        try {
            repository.save(new IssueCouponEntity(message.userId(), message.couponId()));
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 발급 메시지 무시: couponId={}, userId={}", message.couponId(), message.userId());
        }
    }
}
