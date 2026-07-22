package com.saas.subscriptionplatform.event;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.saas.subscriptionplatform.config.KafkaConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionEventProducer {

    private final KafkaTemplate<String, SubscriptionEvent> kafkaTemplate;

    /**
     * 구독 이벤트를 Kafka 토픽으로 발행.
     *
     * 파티션 키로 서비스 계정 ID를 사용해 같은 계정의 이벤트 순서를 보장한다.
     */
    public void publish(String topic, SubscriptionEvent event) {
        String partitionKey = String.valueOf(event.getAccountId());

        try {
            CompletableFuture<SendResult<String, SubscriptionEvent>> future =
                    kafkaTemplate.send(topic, partitionKey, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("이벤트 발행 성공 - topic: {}, accountId: {}, offset: {}",
                            topic, event.getAccountId(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("이벤트 발행 실패 - topic: {}, accountId: {}, error: {}",
                            topic, event.getAccountId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            // Kafka 이벤트 발행 실패가 구독 생성 API 응답에 영향을 주지 않도록 격리
            log.error("이벤트 발행 중 예외 발생 - topic: {}, accountId: {}, error: {}",
                    topic, event.getAccountId(), e.getMessage());
        }
    }

    public void publish(SubscriptionEvent event) {
        switch (event.getEventType()) {
            case CREATED -> publish(KafkaConfig.TOPIC_SUBSCRIPTION_CREATED, event);
            case CANCELLED -> publish(KafkaConfig.TOPIC_SUBSCRIPTION_CANCELLED, event);
            case UPDATED -> publish(KafkaConfig.TOPIC_SUBSCRIPTION_UPDATED, event);
        }
    }
}
