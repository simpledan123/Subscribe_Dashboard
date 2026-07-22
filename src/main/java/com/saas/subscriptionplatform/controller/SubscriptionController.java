package com.saas.subscriptionplatform.controller;

import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/subscriptions") @RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService service;

    public record SubscriptionRequest(Long accountId, Long servicePlanId, String billingCycle, String benefitType,
        LocalDate startDate, LocalDate endDate, LocalDate nextBillingDate, BigDecimal price, Boolean autoRenew) {}

    @GetMapping public List<Subscription> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Subscription findOne(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/account/{accountId}") public List<Subscription> byAccount(@PathVariable Long accountId) { return service.findByAccount(accountId); }
    @PostMapping public Subscription create(@RequestBody SubscriptionRequest b) {
        return service.create(b.accountId(), b.servicePlanId(), b.billingCycle(), b.benefitType(), b.startDate(),
            b.endDate(), b.nextBillingDate(), b.price(), b.autoRenew());
    }
    @PutMapping("/{id}") public Subscription update(@PathVariable Long id, @RequestBody SubscriptionRequest b) {
        return service.update(id, b.servicePlanId(), b.nextBillingDate(), b.endDate(), b.price(), b.autoRenew(), b.benefitType());
    }
    @PutMapping("/{id}/cancel") public Subscription cancel(@PathVariable Long id) { return service.cancel(id); }
}
