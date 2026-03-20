package com.saas.subscriptionplatform.event;

import com.saas.subscriptionplatform.config.KafkaConfig;
import com.saas.subscriptionplatform.entity.Invoice;
import com.saas.subscriptionplatform.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 구독 이벤트 Consumer.
 *
 * 도입 배경:
 *   기존에는 구독 생성 즉시 동기적으로 청구서를 생성했음.
 *   트래픽이 몰리는 시점에 구독 생성 응답이 청구서 생성 처리 시간만큼 지연되는 문제가 있었음.
 *   Kafka를 통해 청구서 생성을 비동기로 분리함으로써 구독 생성 API 응답 시간을 단축하고,
 *   청구 처리 실패가 구독 생성 자체에 영향을 주지 않도록 책임을 분리함.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionEventConsumer {

    private final InvoiceService invoiceService;

    /**
     * 구독 생성 이벤트 수신 → 청구서 자동 생성.
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_SUBSCRIPTION_CREATED,
            groupId = "subscription-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleSubscriptionCreated(SubscriptionEvent event) {
        log.info("구독 생성 이벤트 수신 - subscriptionId: {}, tenantId: {}",
                event.getSubscriptionId(), event.getTenantId());
        try {
            Invoice invoice = invoiceService.generate(event.getSubscriptionId());
            log.info("청구서 자동 생성 완료 - invoiceId: {}, subscriptionId: {}",
                    invoice.getId(), event.getSubscriptionId());
        } catch (Exception e) {
            log.error("청구서 자동 생성 실패 - subscriptionId: {}, error: {}",
                    event.getSubscriptionId(), e.getMessage());
        }
    }

    /**
     * 구독 취소 이벤트 수신 → 취소 처리 로깅 (향후 환불 로직 확장 포인트).
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_SUBSCRIPTION_CANCELLED,
            groupId = "subscription-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleSubscriptionCancelled(SubscriptionEvent event) {
        log.info("구독 취소 이벤트 수신 - subscriptionId: {}, tenantId: {}",
                event.getSubscriptionId(), event.getTenantId());
        // 향후 환불 처리, 알림 발송 등 확장 가능
    }

    /**
     * 플랜 변경 이벤트 수신 → 차액 정산 처리 (향후 프로레이션 로직 확장 포인트).
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_PLAN_CHANGED,
            groupId = "subscription-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePlanChanged(SubscriptionEvent event) {
        log.info("플랜 변경 이벤트 수신 - subscriptionId: {}, 새 planId: {}",
                event.getSubscriptionId(), event.getPlanId());
        // 향후 플랜 변경 차액 청구서 생성 로직 확장 가능
    }
}