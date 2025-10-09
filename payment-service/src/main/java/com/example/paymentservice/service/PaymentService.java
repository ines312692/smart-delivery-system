package com.example.paymentservice.service;


import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentMethod;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessorService paymentProcessorService;

    @Async("paymentProcessorExecutor")
    @Transactional
    public CompletableFuture<Void> initiatePayment(OrderEvent orderEvent) {
        log.info("Initiating payment for order: {}", orderEvent.getOrderNumber());

        // Create payment record
        Payment payment = new Payment();
        payment.setPaymentNumber(generatePaymentNumber());
        payment.setOrderId(orderEvent.getOrderId());
        payment.setOrderNumber(orderEvent.getOrderNumber());
        payment.setCustomerId(orderEvent.getCustomerId());
        payment.setAmount(orderEvent.getTotalAmount());
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD); // Default
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRetryCount(0);
        payment.setMaxRetries(3);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment record created: {}", savedPayment.getPaymentNumber());

        // Process payment asynchronously
        return paymentProcessorService.processPaymentAsync(savedPayment);
    }

    @Transactional
    public void updatePaymentStatus(Long paymentId, PaymentStatus status,
                                    String transactionId, String response) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status);
        payment.setTransactionId(transactionId);
        payment.setGatewayResponse(response);

        if (status == PaymentStatus.COMPLETED) {
            payment.setCompletedAt(LocalDateTime.now());
        } else if (status == PaymentStatus.FAILED) {
            payment.setFailureReason(response);
            if (payment.canRetry()) {
                payment.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
            }
        }

        paymentRepository.save(payment);
        log.info("Payment {} status updated to: {}", payment.getPaymentNumber(), status);
    }

    @Transactional
    public void retryFailedPayment(Payment payment) {
        log.info("Retrying payment: {} (Attempt {}/{})",
                payment.getPaymentNumber(), payment.getRetryCount() + 1, payment.getMaxRetries());

        payment.incrementRetryCount();
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        paymentProcessorService.processPaymentAsync(payment);
    }

    private String generatePaymentNumber() {
        return "PAY-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}