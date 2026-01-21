package com.saas.subscriptionplatform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Plan;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final TenantService tenantService;
    private final PlanService planService;
    
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
        
        return subscriptionRepository.save(subscription);
    }
    
    @Transactional
    public Subscription changePlan(Long subscriptionId, Long newPlanId) {
        Subscription subscription = findById(subscriptionId);
        Plan newPlan = planService.findById(newPlanId);
        subscription.setPlan(newPlan);
        return subscriptionRepository.save(subscription);
    }
    
    @Transactional
    public Subscription cancel(Long id) {
        Subscription subscription = findById(id);
        subscription.setStatus("CANCELLED");
        subscription.setCancelledAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }
}