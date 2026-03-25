package com.saas.subscriptionplatform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Plan;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.event.SubscriptionEvent;
import com.saas.subscriptionplatform.event.SubscriptionEventProducer;
import com.saas.subscriptionplatform.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TenantService tenantService;
    private final PlanService planService;
    private final SubscriptionEventProducer eventProducer;
    private final CacheManager cacheManager;

    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    public Subscription findById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
    }

    public List<Subscription> findByTenant(Long tenantId) {
        Tenant tenant = tenantService.findById(tenantId);
        return subscriptionRepository.findByTenant(tenant);
    }

    @Transactional
    public Subscription create(Long tenantId, Long planId, String billingCycle) {
        Tenant tenant = tenantService.findById(tenantId);
        Plan plan = planService.findById(planId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextBilling = billingCycle.equals("MONTHLY")
                ? now.plusMonths(1)
                : now.plusYears(1);

        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(plan)
                .billingCycle(billingCycle)
                .status("ACTIVE")
                .startDate(now)
                .nextBillingDate(nextBilling)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        SubscriptionEvent event = SubscriptionEvent.builder()
                .eventType(SubscriptionEvent.EventType.CREATED)
                .subscriptionId(saved.getId())
                .tenantId(tenantId)
                .planId(planId)
                .billingCycle(billingCycle)
                .occurredAt(now)
                .build();

        eventProducer.publishCreated(event);
        log.info("구독 생성 이벤트 발행 - subscriptionId: {}", saved.getId());

        return saved;
    }

    @Transactional
    public Subscription changePlan(Long subscriptionId, Long newPlanId) {
        Subscription subscription = findById(subscriptionId);
        Plan newPlan = planService.findById(newPlanId);
        subscription.setPlan(newPlan);
        Subscription saved = subscriptionRepository.save(subscription);

        // planLimits 캐시 무효화 (tenantId 기준)
        var cache = cacheManager.getCache("planLimits");
        if (cache != null) {
            cache.evict(saved.getTenant().getId());
        }

        SubscriptionEvent event = SubscriptionEvent.builder()
                .eventType(SubscriptionEvent.EventType.PLAN_CHANGED)
                .subscriptionId(saved.getId())
                .tenantId(saved.getTenant().getId())
                .planId(newPlanId)
                .billingCycle(saved.getBillingCycle())
                .occurredAt(LocalDateTime.now())
                .build();

        eventProducer.publishPlanChanged(event);
        log.info("플랜 변경 이벤트 발행 - subscriptionId: {}, 새 planId: {}", saved.getId(), newPlanId);

        return saved;
    }

    @Transactional
    public Subscription cancel(Long id) {
        Subscription subscription = findById(id);
        subscription.setStatus("CANCELLED");
        subscription.setCancelledAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(subscription);

        // planLimits 캐시 무효화 (tenantId 기준)
        var cache = cacheManager.getCache("planLimits");
        if (cache != null) {
            cache.evict(saved.getTenant().getId());
        }

        SubscriptionEvent event = SubscriptionEvent.builder()
                .eventType(SubscriptionEvent.EventType.CANCELLED)
                .subscriptionId(saved.getId())
                .tenantId(saved.getTenant().getId())
                .planId(saved.getPlan().getId())
                .billingCycle(saved.getBillingCycle())
                .occurredAt(LocalDateTime.now())
                .build();

        eventProducer.publishCancelled(event);
        log.info("구독 취소 이벤트 발행 - subscriptionId: {}", saved.getId());

        return saved;
    }
}