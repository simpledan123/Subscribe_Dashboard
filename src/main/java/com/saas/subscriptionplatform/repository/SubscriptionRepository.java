package com.saas.subscriptionplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByTenant(Tenant tenant);
    List<Subscription> findByStatus(String status);
}