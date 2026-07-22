package com.saas.subscriptionplatform.event;

import com.saas.subscriptionplatform.config.KafkaConfig;
import com.saas.subscriptionplatform.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j @Component @RequiredArgsConstructor
public class SubscriptionEventConsumer {
    private final PaymentRecordService paymentService;

    @KafkaListener(topics = KafkaConfig.TOPIC_SUBSCRIPTION_CREATED, groupId = "personal-subscription-group")
    public void created(SubscriptionEvent event) {
        try {
            paymentService.schedule(event.getSubscriptionId(), null);
        } catch (IllegalArgumentException freeSubscription) {
            log.info("무료 구독은 결제 일정을 만들지 않습니다. subscriptionId={}", event.getSubscriptionId());
        } catch (Exception e) {
            log.error("결제 일정 생성 실패. subscriptionId={}", event.getSubscriptionId(), e);
        }
    }

    @KafkaListener(topics = {KafkaConfig.TOPIC_SUBSCRIPTION_UPDATED, KafkaConfig.TOPIC_SUBSCRIPTION_CANCELLED},
        groupId = "personal-subscription-group")
    public void changed(SubscriptionEvent event) {
        log.info("구독 변경 이벤트 수신. type={}, subscriptionId={}", event.getEventType(), event.getSubscriptionId());
        if (event.getEventType() == SubscriptionEvent.EventType.CANCELLED) {
            paymentService.skipScheduled(event.getSubscriptionId());
        }
    }
}
