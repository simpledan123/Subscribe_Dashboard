package com.saas.subscriptionplatform.controller;

import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @GetMapping
    public List<Subscription> findAll() {
        return subscriptionService.findAll();
    }
    
    @GetMapping("/{id}")
    public Subscription findById(@PathVariable Long id) {
        return subscriptionService.findById(id);
    }
    
    @GetMapping("/tenant/{tenantId}")
    public List<Subscription> findByTenant(@PathVariable Long tenantId) {
        return subscriptionService.findByTenant(tenantId);
    }
    
    @PostMapping
    public Subscription create(@RequestBody Map<String, Object> request) {
        Long tenantId = Long.valueOf(request.get("tenantId").toString());
        Long planId = Long.valueOf(request.get("planId").toString());
        String billingCycle = request.get("billingCycle").toString();
        return subscriptionService.create(tenantId, planId, billingCycle);
    }
    
    @PutMapping("/{id}/change-plan")
    public Subscription changePlan(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        return subscriptionService.changePlan(id, request.get("planId"));
    }
    
    @PutMapping("/{id}/cancel")
    public Subscription cancel(@PathVariable Long id) {
        return subscriptionService.cancel(id);
    }
}