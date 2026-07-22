package com.saas.subscriptionplatform.service;

import com.saas.subscriptionplatform.entity.ServicePlan;
import com.saas.subscriptionplatform.exception.ResourceNotFoundException;
import com.saas.subscriptionplatform.repository.ServicePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ServicePlanService {
    private final ServicePlanRepository repository;
    public List<ServicePlan> findAll() { return repository.findAll(); }

    @Cacheable(value = "servicePlans", key = "#id")
    public ServicePlan findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("서비스를 찾을 수 없습니다."));
    }

    @Transactional public ServicePlan create(ServicePlan plan) { return repository.save(plan); }

    @Transactional @CacheEvict(value = "servicePlans", key = "#id")
    public ServicePlan update(Long id, ServicePlan input) {
        ServicePlan saved = findById(id);
        saved.setServiceName(input.getServiceName());
        saved.setPlanName(input.getPlanName());
        saved.setCategory(input.getCategory());
        saved.setDescription(input.getDescription());
        saved.setHomepageUrl(input.getHomepageUrl());
        saved.setMonthlyPrice(input.getMonthlyPrice());
        saved.setYearlyPrice(input.getYearlyPrice());
        saved.setUsageLimit(input.getUsageLimit());
        saved.setUsageUnit(input.getUsageUnit());
        saved.setActive(input.getActive() == null ? saved.getActive() : input.getActive());
        return repository.save(saved);
    }

    @Transactional @CacheEvict(value = "servicePlans", key = "#id")
    public void delete(Long id) { repository.delete(findById(id)); }
}
