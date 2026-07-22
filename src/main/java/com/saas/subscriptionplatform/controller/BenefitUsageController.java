package com.saas.subscriptionplatform.controller;

import com.saas.subscriptionplatform.entity.BenefitUsage;
import com.saas.subscriptionplatform.service.BenefitUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/benefits") @RequiredArgsConstructor
public class BenefitUsageController {
    private final BenefitUsageService service;
    public record BenefitRequest(Long subscriptionId, Long usedAmount, Long limitAmount, String unit, LocalDate resetDate, Long amount) {}

    @GetMapping public List<BenefitUsage> findAll() { return service.findAll(); }
    @PostMapping public BenefitUsage save(@RequestBody BenefitRequest b) {
        return service.save(b.subscriptionId(), b.usedAmount(), b.limitAmount(), b.unit(), b.resetDate());
    }
    @PostMapping("/{subscriptionId}/add") public BenefitUsage add(@PathVariable Long subscriptionId, @RequestBody BenefitRequest b) {
        return service.addUsage(subscriptionId, b.amount() == null ? 1 : b.amount());
    }
}
