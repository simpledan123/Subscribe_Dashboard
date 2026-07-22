package com.saas.subscriptionplatform.repository;

import com.saas.subscriptionplatform.entity.ServicePlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicePlanRepository extends JpaRepository<ServicePlan, Long> {
}
