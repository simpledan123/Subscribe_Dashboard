package com.saas.subscriptionplatform.repository;

import com.saas.subscriptionplatform.entity.BenefitUsage;
import com.saas.subscriptionplatform.entity.Subscription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BenefitUsageRepository extends JpaRepository<BenefitUsage, Long> {
    @Override
    @EntityGraph(attributePaths = {"subscription", "subscription.account", "subscription.servicePlan"})
    List<BenefitUsage> findAll();

    Optional<BenefitUsage> findBySubscription(Subscription subscription);
}
