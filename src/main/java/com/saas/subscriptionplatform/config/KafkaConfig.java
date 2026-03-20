package com.saas.subscriptionplatform.config;

import com.saas.subscriptionplatform.event.SubscriptionEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
public class KafkaConfig {

    // 토픽 이름 상수
    public static final String TOPIC_SUBSCRIPTION_CREATED  = "subscription.created";
    public static final String TOPIC_SUBSCRIPTION_CANCELLED = "subscription.cancelled";
    public static final String TOPIC_PLAN_CHANGED          = "subscription.plan-changed";

    /**
     * 토픽 자동 생성.
     * partition 3개: 향후 Consumer를 수평 확장할 때 병렬 처리 가능하도록 여유를 둠.
     */
    @Bean
    public NewTopic subscriptionCreatedTopic() {
        return TopicBuilder.name(TOPIC_SUBSCRIPTION_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic subscriptionCancelledTopic() {
        return TopicBuilder.name(TOPIC_SUBSCRIPTION_CANCELLED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic planChangedTopic() {
        return TopicBuilder.name(TOPIC_PLAN_CHANGED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public RecordMessageConverter converter() {
        return new JsonMessageConverter();
    }
}