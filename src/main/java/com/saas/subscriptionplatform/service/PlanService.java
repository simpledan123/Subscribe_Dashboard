package com.saas.subscriptionplatform.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Plan;
import com.saas.subscriptionplatform.repository.PlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)


public class PlanService {


    private final PlanRepository planRepository;

    public List<Plan> findAll() {
        return planRepository.findAll();
    }

    /**
     * 플랜 단건 조회에 캐시 적용.
     *
     * 도입 배경:
     *   API 호출 한도 체크 시 매 요청마다 Plan을 DB에서 조회하고 있었음.
     *   플랜 데이터는 생성/수정 빈도가 낮아 캐싱 효과가 높은 대상으로 판단.
     *   Redis 캐시 적용 후 반복 조회 시 DB 쿼리가 제거되고 응답시간이 단축됨.
     *
     * TTL: 1시간 (RedisConfig에서 설정)
     */
    @Cacheable(value = "plans", key = "#id")
    public Plan findById(Long id) {
        log.debug("DB에서 Plan 조회 - id: {}", id);
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }

    @Transactional
    @CacheEvict(value = "plans", key = "#result.id")
    public Plan create(Plan plan) {
        return planRepository.save(plan);
    }

    /**
     * 플랜 수정 시 캐시 무효화.
     * 수정된 플랜이 캐시에 남아 구 데이터를 반환하는 문제를 방지.
     */
    @Transactional
    @CacheEvict(value = "plans", key = "#id")
    public Plan update(Long id, Plan updated) {
        log.info("Plan 수정으로 캐시 무효화 - id: {}", id);
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setName(updated.getName());
        plan.setDescription(updated.getDescription());
        plan.setMonthlyPrice(updated.getMonthlyPrice());
        plan.setYearlyPrice(updated.getYearlyPrice());
        plan.setMaxApiCalls(updated.getMaxApiCalls());
        plan.setMaxStorage(updated.getMaxStorage());
        plan.setMaxUsers(updated.getMaxUsers());
        return planRepository.save(plan);
    }

    @Transactional
    @CacheEvict(value = "plans", key = "#id")
    public void delete(Long id) {
        log.info("Plan 삭제로 캐시 무효화 - id: {}", id);
        planRepository.deleteById(id);
    }

	

	
}