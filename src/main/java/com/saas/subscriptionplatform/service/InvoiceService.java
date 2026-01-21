package com.saas.subscriptionplatform.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Invoice;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.entity.Tenant;
import com.saas.subscriptionplatform.repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionService subscriptionService;
    private final TenantService tenantService;
    
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }
    
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }
    
    public List<Invoice> findByTenant(Long tenantId) {
        Tenant tenant = tenantService.findById(tenantId);
        return invoiceRepository.findByTenant(tenant);
    }
    
    @Transactional
    public Invoice generate(Long subscriptionId) {
        Subscription subscription = subscriptionService.findById(subscriptionId);
        
        BigDecimal amount = subscription.getBillingCycle().equals("MONTHLY")
            ? subscription.getPlan().getMonthlyPrice()
            : subscription.getPlan().getYearlyPrice();
        
        LocalDateTime now = LocalDateTime.now();
        String invoiceNumber = "INV-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        
        Invoice invoice = Invoice.builder()
            .tenant(subscription.getTenant())
            .subscription(subscription)
            .invoiceNumber(invoiceNumber)
            .amount(amount)
            .status("PENDING")
            .issueDate(now)
            .dueDate(now.plusDays(30))
            .build();
        
        return invoiceRepository.save(invoice);
    }
    
    @Transactional
    public Invoice markAsPaid(Long id) {
        Invoice invoice = findById(id);
        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }
}