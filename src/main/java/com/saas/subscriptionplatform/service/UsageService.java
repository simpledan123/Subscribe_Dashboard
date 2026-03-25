package com.saas.subscriptionplatform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.entity.Usage;
import com.saas.subscriptionplatform.exception.ApiLimitExceededException;
import com.saas.subscriptionplatform.repository.SubscriptionRepository;
import com.saas.subscriptionplatform.repository.UsageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageService {

    private final UsageRepository usageRepository;
    private final TenantService tenantService;
    private final SubscriptionRepository subscriptionRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private String redisKey(Long tenantId, int year, int month) {
        return String.format("api_calls:%d:%d:%02d", tenantId, year, month);
    }

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

    /**
     * Redis INCR 원자적 카운터로 race condition 해결.
     *
     * 기존 방식:
     *   checkApiLimit() → recordApiCall() 두 트랜잭션 사이에서
     *   동시 요청이 몰릴 경우 한도 초과 케이스가 중복 통과되는 문제가 있었음.
     *
     * 개선:
     *   Redis INCR 단일 원자적 연산으로 카운터 증가와 한도 체크를 분리 불가능한
     *   단일 단계로 처리. 한도 초과 시 즉시 카운터 원복 후 429 반환.
     *
     * Redis-DB 정합성 전략:
     *   Redis → 실시간 rate limiting 전용 (빠른 카운팅)
     *   DB    → 청구 계산 및 영속성 보장용
     *   두 저장소가 일시적으로 다를 수 있으나 청구에는 DB 값만 사용하므로
     *   비즈니스 정합성은 유지됨. Redis 장애 시 DB fallback 처리.
     */
    @Transactional
    public Usage recordApiCallWithCheck(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        String key = redisKey(tenantId, now.getYear(), now.getMonthValue());

        int maxApiCalls = getMaxApiCalls(tenantId);

        Long currentCount;
        try {
            currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount != null && currentCount == 1L) {
                redisTemplate.expire(key, 35, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            // Redis 장애 시 DB fallback
            log.warn("Redis 장애 감지 - DB fallback 처리. tenantId: {}, error: {}",
                    tenantId, e.getMessage());
            return recordApiCallFallback(tenantId, now, maxApiCalls);
        }

        if (currentCount != null && currentCount > maxApiCalls) {
            redisTemplate.opsForValue().decrement(key);
            log.warn("API 호출 한도 초과 - tenantId: {}, count: {}, max: {}",
                    tenantId, currentCount - 1, maxApiCalls);
            throw new ApiLimitExceededException("API 호출 한도를 초과했습니다.");
        }

        return syncUsageToDB(tenantId, now, currentCount != null ? currentCount.intValue() : 0);
    }

    /**
     * Redis 장애 시 DB만으로 한도 체크 및 카운팅.
     * 동시성 보장은 약해지지만 서비스 중단보다 낫다고 판단.
     */
    @Transactional
    private Usage recordApiCallFallback(Long tenantId, LocalDateTime now, int maxApiCalls) {
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

        if (usage.getApiCalls() >= maxApiCalls) {
            throw new ApiLimitExceededException("API 호출 한도를 초과했습니다.");
        }

        usage.setApiCalls(usage.getApiCalls() + 1);
        return usageRepository.save(usage);
    }

    private Usage syncUsageToDB(Long tenantId, LocalDateTime now, int redisCount) {
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

        usage.setApiCalls(redisCount);
        return usageRepository.save(usage);
    }

    @Cacheable(value = "planLimits", key = "#tenantId")
    public int getMaxApiCalls(Long tenantId) {
        Tenant tenant = tenantService.findById(tenantId);
        return subscriptionRepository.findByTenant(tenant).stream()
                .filter(s -> s.getStatus().equals("ACTIVE"))
                .findFirst()
                .map(s -> s.getPlan().getMaxApiCalls())
                .orElseThrow(() -> new RuntimeException("활성 구독이 없습니다."));
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

    public Subscription getActiveSubscription(Long tenantId) {
        Tenant tenant = tenantService.findById(tenantId);
        return subscriptionRepository.findByTenant(tenant).stream()
                .filter(s -> s.getStatus().equals("ACTIVE"))
                .findFirst()
                .orElse(null);
    }

    public void resetMonthlyCounter(Long tenantId, int year, int month) {
        String key = redisKey(tenantId, year, month);
        redisTemplate.delete(key);
        log.info("월별 API 카운터 초기화 완료 - tenantId: {}, {}년 {}월", tenantId, year, month);
    }
}