package com.saas.subscriptionplatform.controller;

import com.saas.subscriptionplatform.entity.PaymentRecord;
import com.saas.subscriptionplatform.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/payments") @RequiredArgsConstructor
public class PaymentRecordController {
    private final PaymentRecordService service;
    public record PaymentRequest(Long subscriptionId, LocalDate scheduledDate, String paymentMethod, String status) {}

    @GetMapping public List<PaymentRecord> findAll() { return service.findAll(); }
    @PostMapping public PaymentRecord schedule(@RequestBody PaymentRequest b) { return service.schedule(b.subscriptionId(), b.scheduledDate()); }
    @PutMapping("/{id}/paid") public PaymentRecord paid(@PathVariable Long id, @RequestBody(required = false) PaymentRequest b) {
        return service.markPaid(id, b == null ? null : b.paymentMethod());
    }
    @PutMapping("/{id}/status") public PaymentRecord status(@PathVariable Long id, @RequestBody PaymentRequest b) {
        return service.updateStatus(id, b.status());
    }
}
