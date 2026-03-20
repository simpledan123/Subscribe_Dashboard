package com.saas.subscriptionplatform.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    /**
     * 고객사 단건 조회에 캐시 적용.
     *
     * 도입 배경:
     *   UsageService에서 API 호출 체크 시 매 요청마다 Tenant를 DB에서 조회하고 있었음.
     *   트래픽이 몰리는 시점에 동일한 Tenant 조회가 반복되는 것을 확인.
     *   TTL을 10분으로 짧게 설정해 상태 변경(SUSPENDED 등)이 비교적 빠르게 반영되도록 함.
     *
     * TTL: 10분 (RedisConfig에서 설정)
     */
    @Cacheable(value = "tenants", key = "#id")
    public Tenant findById(Long id) {
        log.debug("DB에서 Tenant 조회 - id: {}", id);
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }

    @Transactional
    public Tenant create(Tenant tenant) {
        tenant.setStatus("ACTIVE");
        return tenantRepository.save(tenant);
    }

    /**
     * 고객사 수정 시 캐시 무효화.
     * 특히 status 변경(ACTIVE → SUSPENDED)이 즉시 반영되어야 하므로 중요.
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#id")
    public Tenant update(Long id, Tenant updated) {
        log.info("Tenant 수정으로 캐시 무효화 - id: {}", id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setCompanyName(updated.getCompanyName());
        tenant.setEmail(updated.getEmail());
        tenant.setPhone(updated.getPhone());
        return tenantRepository.save(tenant);
    }

    @Transactional
    @CacheEvict(value = "tenants", key = "#id")
    public void delete(Long id) {
        log.info("Tenant 삭제로 캐시 무효화 - id: {}", id);
        tenantRepository.deleteById(id);
    }
}