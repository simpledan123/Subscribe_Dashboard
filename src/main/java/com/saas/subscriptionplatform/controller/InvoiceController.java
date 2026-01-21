package com.saas.subscriptionplatform.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.subscriptionplatform.entity.Invoice;
import com.saas.subscriptionplatform.service.InvoiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @GetMapping
    public List<Invoice> findAll() {
        return invoiceService.findAll();
    }
    
    @GetMapping("/{id}")
    public Invoice findById(@PathVariable Long id) {
        return invoiceService.findById(id);
    }
    
    @GetMapping("/tenant/{tenantId}")
    public List<Invoice> findByTenant(@PathVariable Long tenantId) {
        return invoiceService.findByTenant(tenantId);
    }
    
    @PostMapping
    public Invoice generate(@RequestBody Map<String, Long> request) {
        return invoiceService.generate(request.get("subscriptionId"));
    }
    
    @PutMapping("/{id}/pay")
    public Invoice markAsPaid(@PathVariable Long id) {
        return invoiceService.markAsPaid(id);
    }
}