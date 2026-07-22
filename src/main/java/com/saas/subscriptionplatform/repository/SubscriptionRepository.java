package com.saas.subscriptionplatform.repository;

import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.ServiceAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Override
    @EntityGraph(attributePaths = {"account", "servicePlan"})
    List<Subscription> findAll();

    @Override
    @EntityGraph(attributePaths = {"account", "servicePlan"})
    Optional<Subscription> findById(Long id);

    @EntityGraph(attributePaths = {"account", "servicePlan"})
    List<Subscription> findByAccount(ServiceAccount account);

    @EntityGraph(attributePaths = {"account", "servicePlan"})
    List<Subscription> findByStatus(String status);
}
