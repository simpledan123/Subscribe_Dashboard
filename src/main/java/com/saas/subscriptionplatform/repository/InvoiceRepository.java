package com.saas.subscriptionplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saas.subscriptionplatform.entity.Invoice;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @EntityGraph(attributePaths = {"tenant", "subscription", "subscription.plan"})
    List<Invoice> findAll();

    @EntityGraph(attributePaths = {"tenant", "subscription", "subscription.plan"})
    List<Invoice> findByTenant(Tenant tenant);

    List<Invoice> findByStatus(String status);

    @Query("SELECT COUNT(i) > 0 FROM Invoice i " +
       "WHERE i.subscription = :subscription " +
       "AND YEAR(i.issueDate) = :year " +
       "AND MONTH(i.issueDate) = :month")
    boolean existsBySubscriptionAndYearAndMonth(
            @Param("subscription") Subscription subscription,
            @Param("year") int year,
            @Param("month") int month);

}