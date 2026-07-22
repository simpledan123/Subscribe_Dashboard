package com.saas.subscriptionplatform.repository;

import com.saas.subscriptionplatform.entity.PaymentRecord;
import com.saas.subscriptionplatform.entity.Subscription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    @Override
    @EntityGraph(attributePaths = {"subscription", "subscription.account", "subscription.servicePlan"})
    List<PaymentRecord> findAll();

    @Override
    @EntityGraph(attributePaths = {"subscription", "subscription.account", "subscription.servicePlan"})
    Optional<PaymentRecord> findById(Long id);

    boolean existsBySubscriptionAndScheduledDate(Subscription subscription, LocalDate scheduledDate);
    List<PaymentRecord> findBySubscriptionAndStatus(Subscription subscription, String status);
    List<PaymentRecord> findByStatusAndScheduledDateLessThanEqual(String status, LocalDate date);
}
