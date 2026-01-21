package com.saas.subscriptionplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.subscriptionplatform.entity.Invoice;
import com.saas.subscriptionplatform.entity.Tenant;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByTenant(Tenant tenant);
    List<Invoice> findByStatus(String status);
}