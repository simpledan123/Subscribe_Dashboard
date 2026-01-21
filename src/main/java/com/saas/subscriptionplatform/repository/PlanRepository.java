package com.saas.subscriptionplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.subscriptionplatform.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}