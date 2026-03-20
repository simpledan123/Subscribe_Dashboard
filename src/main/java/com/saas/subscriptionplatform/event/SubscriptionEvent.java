package com.saas.subscriptionplatform.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kafka 이벤트 페이로드.
 *
 * 구독 생성/취소/플랜 변경 시 발행되며,
 * Consumer에서 청구서 자동 생성 등 후속 처리에 활용됨.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEvent {

    public enum EventType {
        CREATED, CANCELLED, PLAN_CHANGED
    }

    private EventType eventType;
    private Long subscriptionId;
    private Long tenantId;
    private Long planId;
    private String billingCycle;
    private LocalDateTime occurredAt;
}