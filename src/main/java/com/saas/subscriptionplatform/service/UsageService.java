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
import com.saas.subscriptionplatform.repository.SubscriptionRepository;
import com.saas.subscriptionplatform.repository.UsageRepository;
import com.saas.subscriptionplatform.exception.ApiLimitExceededException;

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

    // Redis 키: "api_calls:{tenantId}:{yyyy}:{MM}"
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
     * Redis INCR을 사용한 원자적 API 호출 카운트 증가 및 한도 체크.
     *
     * 기존 방식의 문제:
     *   checkApiLimit() → recordApiCall() 이 두 트랜잭션 사이에서
     *   동시 요청이 몰릴 경우 한도 초과 케이스가 중복 통과되는 race condition이 발생했음.
     *
     * 개선:
     *   Redis INCR은 단일 원자적 연산이므로 동시 요청이 몰려도
     *   카운터가 정확하게 1씩만 증가하고, 초과 즉시 차단이 보장됨.
     */
    @Transactional
    public Usage recordApiCallWithCheck(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        String key = redisKey(tenantId, now.getYear(), now.getMonthValue());

        // 활성 구독의 플랜 한도 조회
        int maxApiCalls = getMaxApiCalls(tenantId);

        // INCR: 원자적으로 증가 후 현재 값 반환
        Long currentCount = redisTemplate.opsForValue().increment(key);

        // 처음 생성된 키는 만료 시간 설정 (다음 달 1일까지 유지)
        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(key, 35, TimeUnit.DAYS);
        }

        // 한도 초과 시 즉시 차단
        if (currentCount != null && currentCount > maxApiCalls) {
            // 증가시켰던 카운터 원복
            redisTemplate.opsForValue().decrement(key);
            log.warn("API 호출 한도 초과 - tenantId: {}, count: {}, max: {}",
                    tenantId, currentCount - 1, maxApiCalls);
            throw new RuntimeException("API 호출 한도를 초과했습니다.");
        }

        // DB Usage 테이블 동기화 (캐시와 DB 정합성 유지)
        return syncUsageToDB(tenantId, now, currentCount != null ? currentCount.intValue() : 0);
    }

    /**
     * DB Usage 레코드와 Redis 카운터를 동기화.
     * DB는 영속성 보장 및 청구 계산용, Redis는 실시간 카운팅용으로 역할을 분리.
     */
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

// 기존 getMaxApiCalls() 교체
    @Cacheable(value = "planLimits", key = "#tenantId")
    private int getMaxApiCalls(Long tenantId) {
        Tenant tenant = tenantService.findById(tenantId);
        return subscriptionRepository.findByTenant(tenant).stream()
                .filter(s -> s.getStatus().equals("ACTIVE"))
                .findFirst()
                .map(s -> s.getPlan().getMaxApiCalls())
                .orElseThrow(() -> new RuntimeException("활성 구독이 없습니다."));
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

    public Subscription getActiveSubscription(Long tenantId) {
        Tenant tenant = tenantService.findById(tenantId);
        return subscriptionRepository.findByTenant(tenant).stream()
                .filter(s -> s.getStatus().equals("ACTIVE"))
                .findFirst()
                .orElse(null);
    }

    /**
     * 월 초기화 시 Redis 카운터 리셋 (Spring Batch에서 호출 예정).
     */
    public void resetMonthlyCounter(Long tenantId, int year, int month) {
        String key = redisKey(tenantId, year, month);
        redisTemplate.delete(key);
        log.info("월별 API 카운터 초기화 완료 - tenantId: {}, {}년 {}월", tenantId, year, month);
    }
}