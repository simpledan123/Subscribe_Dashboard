package com.saas.subscriptionplatform.service;

import com.saas.subscriptionplatform.entity.PaymentRecord;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.exception.ResourceNotFoundException;
import com.saas.subscriptionplatform.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class PaymentRecordService {
    private final PaymentRecordRepository repository;
    private final SubscriptionService subscriptionService;

    public List<PaymentRecord> findAll() { return repository.findAll(); }
    public PaymentRecord findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("결제 기록을 찾을 수 없습니다."));
    }

    @Transactional
    public PaymentRecord schedule(Long subscriptionId, LocalDate scheduledDate) {
        Subscription sub = subscriptionService.findById(subscriptionId);
        LocalDate date = scheduledDate != null ? scheduledDate : sub.getNextBillingDate();
        if (date == null) throw new IllegalArgumentException("무료 구독에는 결제 예정일이 없습니다.");
        if (repository.existsBySubscriptionAndScheduledDate(sub, date)) {
            return repository.findAll().stream()
                .filter(p -> p.getSubscription().getId().equals(subscriptionId) && p.getScheduledDate().equals(date))
                .findFirst().orElseThrow();
        }
        return repository.save(PaymentRecord.builder().subscription(sub).amount(sub.getPrice())
            .status("SCHEDULED").scheduledDate(date).build());
    }

    @Transactional
    public PaymentRecord markPaid(Long id, String paymentMethod) {
        PaymentRecord payment = findById(id);
        if ("PAID".equals(payment.getStatus())) return payment;
        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        PaymentRecord saved = repository.save(payment);
        subscriptionService.advanceBillingDate(payment.getSubscription().getId(), payment.getScheduledDate());
        return saved;
    }

    @Transactional
    public PaymentRecord updateStatus(Long id, String status) {
        PaymentRecord payment = findById(id);
        payment.setStatus(status);
        return repository.save(payment);
    }

    @Transactional
    public void skipScheduled(Long subscriptionId) {
        Subscription subscription = subscriptionService.findById(subscriptionId);
        repository.findBySubscriptionAndStatus(subscription, "SCHEDULED").forEach(payment -> payment.setStatus("SKIPPED"));
    }
}
