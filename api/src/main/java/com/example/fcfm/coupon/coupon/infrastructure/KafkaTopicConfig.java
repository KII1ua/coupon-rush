package com.example.fcfm.coupon.coupon.infrastructure;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic couponIssuedTopic() {
        return TopicBuilder.name(KafkaCouponIssuedPublisher.TOPIC)
                .partitions(3)      // 파티션 키(userId)로 분산 소비
                .replicas(1)
                .build();
    }
}
