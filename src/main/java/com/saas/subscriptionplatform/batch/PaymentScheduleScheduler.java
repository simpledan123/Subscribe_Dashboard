package com.saas.subscriptionplatform.batch;

import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.repository.SubscriptionRepository;
import com.saas.subscriptionplatform.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Slf4j @Component @RequiredArgsConstructor
public class PaymentScheduleScheduler {
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRecordService paymentService;

    @Scheduled(cron = "0 10 0 * * *")
    public void createUpcomingSchedules() {
        LocalDate cutoff = LocalDate.now().plusDays(30);
        int created = 0;
        for (Subscription sub : subscriptionRepository.findByStatus("ACTIVE")) {
            if (sub.getNextBillingDate() == null || sub.getNextBillingDate().isAfter(cutoff)) continue;
            paymentService.schedule(sub.getId(), sub.getNextBillingDate());
            created++;
        }
        log.info("30일 이내 결제 일정 동기화 완료. 대상={}건", created);
    }
}
