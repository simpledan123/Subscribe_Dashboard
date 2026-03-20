package com.saas.subscriptionplatform.batch;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.saas.subscriptionplatform.entity.Invoice;
import com.saas.subscriptionplatform.entity.Subscription;
import com.saas.subscriptionplatform.repository.SubscriptionRepository;
import com.saas.subscriptionplatform.service.InvoiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 월별 청구서 자동 생성 스케줄러.
 *
 * 도입 배경:
 *   기존에는 청구서를 수동으로 생성해야 했음.
 *   매월 1일 자정에 전체 ACTIVE 구독에 대해 자동으로 청구서를 발행하도록 자동화함.
 *   청구서 생성 실패가 전체 배치를 중단시키지 않도록 건별로 예외를 격리해 처리함.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceService invoiceService;

    /**
     * 매월 1일 00:00:00 실행.
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void runMonthlyBilling() {
        LocalDate today = LocalDate.now();
        log.info("월별 청구서 자동 생성 시작 - {}년 {}월", today.getYear(), today.getMonthValue());

        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus("ACTIVE");
        log.info("처리 대상 구독 수: {}", activeSubscriptions.size());

        int successCount = 0;
        int failCount = 0;

        for (Subscription subscription : activeSubscriptions) {
            try {
                Invoice invoice = invoiceService.generate(subscription.getId());
                log.info("청구서 생성 완료 - invoiceId: {}, subscriptionId: {}, tenantId: {}",
                        invoice.getId(), subscription.getId(), subscription.getTenant().getId());
                successCount++;
            } catch (Exception e) {
                // 개별 실패가 전체 처리를 중단시키지 않도록 예외 격리
                log.error("청구서 생성 실패 - subscriptionId: {}, error: {}",
                        subscription.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("월별 청구서 자동 생성 완료 - {}년 {}월, 성공: {}건, 실패: {}건",
                today.getYear(), today.getMonthValue(), successCount, failCount);
    }
}