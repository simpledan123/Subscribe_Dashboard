package com.saas.subscriptionplatform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Plan;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.entity.Usage;
import com.saas.subscriptionplatform.repository.SubscriptionRepository;
import com.saas.subscriptionplatform.repository.UsageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageService {
    
    private final UsageRepository usageRepository;
    private final TenantService tenantService;
    private final SubscriptionRepository subscriptionRepository;
    
    public List<Usage> findAll() {
        return usageRepository.findAll();
    }
    
    public Usage findById(Long id) {
        return usageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usage not found"));
    }
    
    public Usage findByTenantAndMonth(Long tenantId, Integer year, Integer month) {
        Tenant tenant = tenantService.findById(tenantId);
        return usageRepository.findByTenantAndBillingYearAndBillingMonth(tenant, year, month)
            .orElse(null);
    }
    
    @Transactional
    public Usage recordApiCall(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = tenantService.findById(tenantId);
        
        Usage usage = usageRepository
            .findByTenantAndBillingYearAndBillingMonth(tenant, now.getYear(), now.getMonthValue())
            .orElseGet(() -> Usage.builder()
                .tenant(tenant)
                .billingYear(now.getYear())
                .billingMonth(now.getMonthValue())
                .apiCalls(0)
                .storageUsed(0)
                .activeUsers(0)
                .build());
        
        usage.setApiCalls(usage.getApiCalls() + 1);
        return usageRepository.save(usage);
    }
    
    @Transactional
    public Usage updateStorage(Long tenantId, Integer storageUsed) {
        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = tenantService.findById(tenantId);
        
        Usage usage = usageRepository
            .findByTenantAndBillingYearAndBillingMonth(tenant, now.getYear(), now.getMonthValue())
            .orElseThrow(() -> new RuntimeException("Usage not found"));
        
        usage.setStorageUsed(storageUsed);
        return usageRepository.save(usage);
    }

    @Transactional
    public boolean checkApiLimit(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = tenantService.findById(tenantId);
        
        // 현재 월 사용량 조회
        Usage usage = usageRepository
            .findByTenantAndBillingYearAndBillingMonth(tenant, now.getYear(), now.getMonthValue())
            .orElse(null);
        
        if (usage == null) {
            return true; // 사용량 없으면 통과
        }
        
        // 테넌트의 활성 구독 찾기
        List<Subscription> subscriptions = subscriptionRepository.findByTenant(tenant);
        Subscription activeSubscription = subscriptions.stream()
            .filter(s -> s.getStatus().equals("ACTIVE"))
            .findFirst()
            .orElse(null);
        
        if (activeSubscription == null) {
            return false; // 구독 없으면 차단
        }
        
        Plan plan = activeSubscription.getPlan();
        return usage.getApiCalls() < plan.getMaxApiCalls();
    }

    @Transactional
    public Usage recordApiCallWithCheck(Long tenantId) {
        if (!checkApiLimit(tenantId)) {
            throw new RuntimeException("API 호출 한도를 초과했습니다.");
        }
        return recordApiCall(tenantId);
    }   

    public Subscription getActiveSubscription(Long tenantId) {
    Tenant tenant = tenantService.findById(tenantId);
    List<Subscription> subscriptions = subscriptionRepository.findByTenant(tenant);
    return subscriptions.stream()
        .filter(s -> s.getStatus().equals("ACTIVE"))
        .findFirst()
        .orElse(null);
}
}