package com.saas.subscriptionplatform.config;

import com.saas.subscriptionplatform.entity.*;
import com.saas.subscriptionplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component @Profile("demo") @RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {
    private final AppUserRepository users;
    private final ServiceAccountRepository accounts;
    private final ServicePlanRepository plans;
    private final SubscriptionRepository subscriptions;
    private final PaymentRecordRepository payments;
    private final BenefitUsageRepository benefits;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            users.save(AppUser.builder().username("demo").password(passwordEncoder.encode("demo1234")).build());
        }
        if (accounts.count() > 0) return;

        ServiceAccount main = accounts.save(ServiceAccount.builder().nickname("메인 계정")
            .email("me@example.com").purpose("PERSONAL").memo("일상 구독 결제용").build());
        ServiceAccount student = accounts.save(ServiceAccount.builder().nickname("학생 혜택 계정")
            .email("student@example.ac.kr").purpose("STUDENT_BENEFIT").memo("졸업 전 혜택 만료일 확인").build());
        ServiceAccount career = accounts.save(ServiceAccount.builder().nickname("취업 준비 계정")
            .email("career@example.com").purpose("JOB_SEARCH").build());

        ServicePlan chatgpt = plans.save(plan("ChatGPT", "Plus", "AI", 29000, 290000, 80L, "MESSAGES"));
        ServicePlan github = plans.save(plan("GitHub", "Student Developer Pack", "DEVELOPMENT", 0, 0, null, null));
        ServicePlan jobplanet = plans.save(plan("잡플래닛", "멤버십", "CAREER", 9900, 99000, null, null));
        ServicePlan notion = plans.save(plan("Notion", "Education Plus", "PRODUCTIVITY", 0, 0, 5L, "GB"));

        LocalDate today = LocalDate.now();
        Subscription ai = subscriptions.save(subscription(main, chatgpt, "MONTHLY", "PAID", today.minusMonths(3),
            null, today.plusDays(8), 29000, true));
        Subscription studentPack = subscriptions.save(subscription(student, github, "FREE", "STUDENT", today.minusYears(1),
            today.plusMonths(5), null, 0, false));
        Subscription job = subscriptions.save(subscription(career, jobplanet, "MONTHLY", "PAID", today.minusMonths(1),
            null, today.plusDays(3), 9900, true));
        Subscription edu = subscriptions.save(subscription(student, notion, "FREE", "STUDENT", today.minusMonths(8),
            today.plusMonths(5), null, 0, false));

        payments.saveAll(List.of(
            payment(ai, 29000, "PAID", today.minusMonths(1), "체크카드"),
            payment(ai, 29000, "PAID", today.minusMonths(2), "체크카드"),
            payment(job, 9900, "SCHEDULED", today.plusDays(3), null),
            payment(ai, 29000, "SCHEDULED", today.plusDays(8), null)
        ));
        benefits.save(BenefitUsage.builder().subscription(ai).usedAmount(46L).limitAmount(80L)
            .unit("MESSAGES").resetDate(today.plusDays(8)).build());
        benefits.save(BenefitUsage.builder().subscription(edu).usedAmount(2L).limitAmount(5L)
            .unit("GB").resetDate(today.plusMonths(1)).build());
        benefits.save(BenefitUsage.builder().subscription(studentPack).usedAmount(0L).unit("BENEFIT")
            .resetDate(studentPack.getEndDate()).build());
    }

    private ServicePlan plan(String service, String plan, String category, long monthly, long yearly, Long limit, String unit) {
        return ServicePlan.builder().serviceName(service).planName(plan).category(category)
            .monthlyPrice(BigDecimal.valueOf(monthly)).yearlyPrice(BigDecimal.valueOf(yearly))
            .usageLimit(limit).usageUnit(unit).description(service + " 개인 구독 플랜").build();
    }

    private Subscription subscription(ServiceAccount account, ServicePlan plan, String cycle, String benefit,
                                      LocalDate start, LocalDate end, LocalDate next, long price, boolean renew) {
        return Subscription.builder().account(account).servicePlan(plan).billingCycle(cycle).benefitType(benefit)
            .status("ACTIVE").startDate(start).endDate(end).nextBillingDate(next)
            .price(BigDecimal.valueOf(price)).autoRenew(renew).build();
    }

    private PaymentRecord payment(Subscription sub, long amount, String status, LocalDate date, String method) {
        return PaymentRecord.builder().subscription(sub).amount(BigDecimal.valueOf(amount)).status(status)
            .scheduledDate(date).paidAt("PAID".equals(status) ? LocalDateTime.now().minusDays(5) : null)
            .paymentMethod(method).build();
    }
}
