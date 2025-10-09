package com.example.paymentservice.service;


import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryPaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 60000) // Run every minute
    @Async("retryExecutor")
    public void retryFailedPayments() {
        log.info("Checking for failed payments to retry...");

        List<Payment> paymentsToRetry = paymentRepository
                .findPendingRetries(PaymentStatus.FAILED, LocalDateTime.now());

        if (paymentsToRetry.isEmpty()) {
            log.info("No payments to retry at this time");
            return;
        }

        log.info("Found {} payments to retry", paymentsToRetry.size());

        for (Payment payment : paymentsToRetry) {
            if (payment.canRetry()) {
                try {
                    paymentService.retryFailedPayment(payment);
                } catch (Exception e) {
                    log.error("Error retrying payment: {}", payment.getPaymentNumber(), e);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void logPaymentStatistics() {
        long pending = paymentRepository.findByStatus(PaymentStatus.PENDING).size();
        long processing = paymentRepository.findByStatus(PaymentStatus.PROCESSING).size();
        long completed = paymentRepository.findByStatus(PaymentStatus.COMPLETED).size();
        long failed = paymentRepository.findByStatus(PaymentStatus.FAILED).size();

        log.info("Payment Statistics - Pending: {}, Processing: {}, Completed: {}, Failed: {}",
                pending, processing, completed, failed);
    }
}
