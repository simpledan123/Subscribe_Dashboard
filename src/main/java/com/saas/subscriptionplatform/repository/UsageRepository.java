package com.saas.subscriptionplatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.entity.Usage;

public interface UsageRepository extends JpaRepository<Usage, Long> {
    Optional<Usage> findByTenantAndBillingYearAndBillingMonth(Tenant tenant, Integer year, Integer month);
}