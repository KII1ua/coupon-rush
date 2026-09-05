package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.CouponIssuedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class KafkaCouponIssuedPublisher implements CouponIssuedPublisher {
    public static final String TOPIC = "coupon-issued";

    private final KafkaTemplate<String, CouponIssuedMessage> kafkaTemplate;

    @Override
    public void publish(Long couponId, Long userId) {
        try {
            // 브로커 수신 확인(ack)까지 기다린다. 응답의 "발급 완료"가 최소한 "큐에 안전히 들어감"을 의미하도록.
            kafkaTemplate.send(TOPIC, String.valueOf(userId), new CouponIssuedMessage(couponId, userId))
                    .get(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("발급 메시지 발행이 중단되었습니다.", e);
        } catch (Exception e) {
            throw new IllegalStateException("발급 메시지 발행에 실패했습니다.", e);
        }
    }
}
