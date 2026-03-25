package com.saas.subscriptionplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @EntityGraph(attributePaths = {"tenant", "plan"})
    List<Subscription> findAll();

    @EntityGraph(attributePaths = {"tenant", "plan"})
    List<Subscription> findByTenant(Tenant tenant);

    @EntityGraph(attributePaths = {"tenant", "plan"})
    List<Subscription> findByStatus(String status);
}