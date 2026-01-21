package com.saas.subscriptionplatform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Plan;
import com.saas.subscriptionplatform.repository.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {
    
    private final PlanRepository planRepository;
    
    public List<Plan> findAll() {
        return planRepository.findAll();
    }
    
    public Plan findById(Long id) {
        return planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
    }
    
    @Transactional
    public Plan create(Plan plan) {
        return planRepository.save(plan);
    }
    
    @Transactional
    public Plan update(Long id, Plan updated) {
        Plan plan = findById(id);
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
    public void delete(Long id) {
        planRepository.deleteById(id);
    }
}