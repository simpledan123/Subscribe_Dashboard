package com.saas.subscriptionplatform.service;

import com.saas.subscriptionplatform.entity.*;
import com.saas.subscriptionplatform.event.SubscriptionEvent;
import com.saas.subscriptionplatform.event.SubscriptionEventProducer;
import com.saas.subscriptionplatform.exception.ResourceNotFoundException;
import com.saas.subscriptionplatform.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class SubscriptionService {
    private final SubscriptionRepository repository;
    private final ServiceAccountService accountService;
    private final ServicePlanService planService;
    private final SubscriptionEventProducer eventProducer;

    public List<Subscription> findAll() { return repository.findAll(); }
    public Subscription findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("구독을 찾을 수 없습니다."));
    }
    public List<Subscription> findByAccount(Long accountId) {
        return repository.findByAccount(accountService.findById(accountId));
    }

    @Transactional
    public Subscription create(Long accountId, Long planId, String billingCycle, String benefitType,
                               LocalDate startDate, LocalDate endDate, LocalDate nextBillingDate,
                               BigDecimal price, Boolean autoRenew) {
        ServiceAccount account = accountService.findById(accountId);
        ServicePlan plan = planService.findById(planId);
        LocalDate start = startDate == null ? LocalDate.now() : startDate;
        String cycle = billingCycle == null ? "MONTHLY" : billingCycle;
        BigDecimal actualPrice = price == null ? defaultPrice(plan, cycle) : price;
        LocalDate next = nextBillingDate;
        if (next == null && !"FREE".equals(cycle)) {
            next = "YEARLY".equals(cycle) ? start.plusYears(1) : start.plusMonths(1);
        }

        Subscription saved = repository.save(Subscription.builder()
            .account(account).servicePlan(plan).billingCycle(cycle).benefitType(benefitType == null ? "PAID" : benefitType)
            .status("ACTIVE").startDate(start).endDate(endDate).nextBillingDate(next).price(actualPrice)
            .autoRenew(autoRenew == null || autoRenew).build());
        publish(saved, SubscriptionEvent.EventType.CREATED);
        return saved;
    }

    @Transactional
    public Subscription update(Long id, Long planId, LocalDate nextBillingDate, LocalDate endDate,
                               BigDecimal price, Boolean autoRenew, String benefitType) {
        Subscription subscription = findById(id);
        if (planId != null) subscription.setServicePlan(planService.findById(planId));
        if (nextBillingDate != null) subscription.setNextBillingDate(nextBillingDate);
        subscription.setEndDate(endDate);
        if (price != null) subscription.setPrice(price);
        if (autoRenew != null) subscription.setAutoRenew(autoRenew);
        if (benefitType != null) subscription.setBenefitType(benefitType);
        Subscription saved = repository.save(subscription);
        publish(saved, SubscriptionEvent.EventType.UPDATED);
        return saved;
    }

    @Transactional
    public Subscription cancel(Long id) {
        Subscription subscription = findById(id);
        subscription.setStatus("CANCELLED");
        subscription.setAutoRenew(false);
        subscription.setCancelledAt(LocalDateTime.now());
        Subscription saved = repository.save(subscription);
        publish(saved, SubscriptionEvent.EventType.CANCELLED);
        return saved;
    }

    @Transactional
    public Subscription advanceBillingDate(Long id, LocalDate completedDate) {
        Subscription subscription = findById(id);
        if ("MONTHLY".equals(subscription.getBillingCycle())) {
            subscription.setNextBillingDate(completedDate.plusMonths(1));
        } else if ("YEARLY".equals(subscription.getBillingCycle())) {
            subscription.setNextBillingDate(completedDate.plusYears(1));
        }
        return repository.save(subscription);
    }

    private BigDecimal defaultPrice(ServicePlan plan, String cycle) {
        if ("FREE".equals(cycle)) return BigDecimal.ZERO;
        return "YEARLY".equals(cycle) ? plan.getYearlyPrice() : plan.getMonthlyPrice();
    }

    private void publish(Subscription saved, SubscriptionEvent.EventType type) {
        SubscriptionEvent event = SubscriptionEvent.builder().eventType(type).subscriptionId(saved.getId())
            .accountId(saved.getAccount().getId()).servicePlanId(saved.getServicePlan().getId())
            .billingCycle(saved.getBillingCycle()).occurredAt(LocalDateTime.now()).build();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { eventProducer.publish(event); }
            });
        } else {
            eventProducer.publish(event);
        }
    }
}
