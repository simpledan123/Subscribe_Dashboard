package com.saas.subscriptionplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.saas.subscriptionplatform.event.SubscriptionEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_SUBSCRIPTION_CREATED   = "subscription.created";
    public static final String TOPIC_SUBSCRIPTION_CANCELLED = "subscription.cancelled";
    public static final String TOPIC_SUBSCRIPTION_UPDATED   = "subscription.updated";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * ProducerFactory 명시적 정의.
     * LocalDateTime 직렬화를 위해 JavaTimeModule을 등록한 ObjectMapper를 사용.
     */
    @Bean
    public ProducerFactory<String, SubscriptionEvent> producerFactory() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DefaultKafkaProducerFactory<String, SubscriptionEvent> factory =
                new DefaultKafkaProducerFactory<>(config);
        factory.setValueSerializer(new JsonSerializer<>(objectMapper));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, SubscriptionEvent> kafkaTemplate(
            ProducerFactory<String, SubscriptionEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

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
    public NewTopic subscriptionUpdatedTopic() {
        return TopicBuilder.name(TOPIC_SUBSCRIPTION_UPDATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
