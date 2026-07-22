package com.saas.subscriptionplatform.service;

import com.saas.subscriptionplatform.entity.BenefitUsage;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.exception.ResourceNotFoundException;
import com.saas.subscriptionplatform.repository.BenefitUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class BenefitUsageService {
    private final BenefitUsageRepository repository;
    private final SubscriptionService subscriptionService;
    private final RedisTemplate<String, String> redisTemplate;

    public List<BenefitUsage> findAll() { return repository.findAll(); }

    @Transactional
    public BenefitUsage save(Long subscriptionId, Long usedAmount, Long limitAmount, String unit, LocalDate resetDate) {
        Subscription sub = subscriptionService.findById(subscriptionId);
        BenefitUsage usage = repository.findBySubscription(sub).orElseGet(() -> BenefitUsage.builder()
            .subscription(sub).usedAmount(0L).build());
        usage.setUsedAmount(usedAmount == null ? usage.getUsedAmount() : usedAmount);
        usage.setLimitAmount(limitAmount != null ? limitAmount : sub.getServicePlan().getUsageLimit());
        usage.setUnit(unit != null ? unit : sub.getServicePlan().getUsageUnit());
        usage.setResetDate(resetDate);
        return repository.save(usage);
    }

    @Transactional
    public BenefitUsage addUsage(Long subscriptionId, long amount) {
        Subscription sub = subscriptionService.findById(subscriptionId);
        BenefitUsage usage = repository.findBySubscription(sub)
            .orElseThrow(() -> new ResourceNotFoundException("먼저 사용량 기준을 등록해 주세요."));
        String key = "benefit-usage:" + subscriptionId;
        try {
            Long cached = redisTemplate.opsForValue().increment(key, amount);
            if (cached != null && cached == amount) {
                redisTemplate.expire(key, 35, TimeUnit.DAYS);
                cached = usage.getUsedAmount() + amount;
                redisTemplate.opsForValue().set(key, String.valueOf(cached), 35, TimeUnit.DAYS);
            }
            usage.setUsedAmount(cached == null ? usage.getUsedAmount() + amount : cached);
        } catch (Exception ignored) {
            usage.setUsedAmount(usage.getUsedAmount() + amount);
        }
        return repository.save(usage);
    }
}
