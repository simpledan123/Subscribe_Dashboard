package com.saas.subscriptionplatform.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.saas.subscriptionplatform.entity.Usage;
import com.saas.subscriptionplatform.service.UsageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usages")
@RequiredArgsConstructor
public class UsageController {
    
    private final UsageService usageService;
    
    @GetMapping
    public List<Usage> findAll() {
        return usageService.findAll();
    }
    
    @GetMapping("/{id}")
    public Usage findById(@PathVariable Long id) {
        return usageService.findById(id);
    }
    
    @GetMapping("/tenant/{tenantId}")
    public Usage findByTenantAndMonth(
            @PathVariable Long tenantId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return usageService.findByTenantAndMonth(tenantId, year, month);
    }
    
    @PostMapping("/tenant/{tenantId}/api-call")
    public ResponseEntity<?> recordApiCall(@PathVariable Long tenantId) {
        try {
            Usage usage = usageService.recordApiCallWithCheck(tenantId);
            return ResponseEntity.ok(usage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(429).body(Map.of(
                "error", e.getMessage(),
                "code", "LIMIT_EXCEEDED"
            ));
        }
    }
    
    @PutMapping("/tenant/{tenantId}/storage")
    public Usage updateStorage(@PathVariable Long tenantId, @RequestBody Map<String, Integer> request) {
        return usageService.updateStorage(tenantId, request.get("storageUsed"));
    }
    @GetMapping("/tenant/{tenantId}/status")
    public ResponseEntity<?> getUsageStatus(@PathVariable Long tenantId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Usage usage = usageService.findByTenantAndMonth(tenantId, now.getYear(), now.getMonthValue());
            
            // 구독 정보에서 플랜 한도 가져오기
            var subscriptions = usageService.getActiveSubscription(tenantId);
            
            Map<String, Object> status = new HashMap<>();
            status.put("currentUsage", usage != null ? usage.getApiCalls() : 0);
            status.put("usage", usage);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}