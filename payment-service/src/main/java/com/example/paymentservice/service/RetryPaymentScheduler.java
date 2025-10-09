package com.example.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryPaymentScheduler {

    private final PaymentService paymentService;

    @Scheduled(fixedRate = 60000)
    public void retryFailedPayments() {
        log.info("Retrying failed payments...");
        paymentService.retryFailedPayments();
    }
}
