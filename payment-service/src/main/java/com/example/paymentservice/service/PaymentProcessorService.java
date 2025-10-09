package com.example.paymentservice.service;


import com.example.paymentservice.dto.PaymentEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorService {

    private final PaymentService paymentService;
    private final KafkaProducerService kafkaProducerService;
    private final Random random = new Random();

    @Async("paymentProcessorExecutor")
    public CompletableFuture<Void> processPaymentAsync(Payment payment) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Processing payment: {}", payment.getPaymentNumber());

                // Update to processing
                paymentService.updatePaymentStatus(
                        payment.getId(),
                        PaymentStatus.PROCESSING,
                        null,
                        "Payment processing started"
                );

                // Simulate payment gateway processing time
                TimeUnit.SECONDS.sleep(2);

                // Simulate payment gateway response (80% success rate)
                boolean success = random.nextInt(100) < 80;

                if (success) {
                    handleSuccessfulPayment(payment);
                } else {
                    handleFailedPayment(payment);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Payment processing interrupted: {}", payment.getPaymentNumber());
                handleFailedPayment(payment);
            } catch (Exception e) {
                log.error("Error processing payment: {}", payment.getPaymentNumber(), e);
                handleFailedPayment(payment);
            }
        });
    }

    private void handleSuccessfulPayment(Payment payment) {
        String transactionId = "TXN-" + UUID.randomUUID().toString();

        paymentService.updatePaymentStatus(
                payment.getId(),
                PaymentStatus.COMPLETED,
                transactionId,
                "Payment completed successfully"
        );

        // Publish payment completed event
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_COMPLETED")
                .paymentId(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(PaymentStatus.COMPLETED)
                .transactionId(transactionId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducerService.sendPaymentCompletedEvent(event);
        log.info("Payment completed successfully: {}", payment.getPaymentNumber());
    }

    private void handleFailedPayment(Payment payment) {
        String failureReason = "Payment gateway declined the transaction";

        paymentService.updatePaymentStatus(
                payment.getId(),
                PaymentStatus.FAILED,
                null,
                failureReason
        );

        // Publish payment failed event
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_FAILED")
                .paymentId(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(PaymentStatus.FAILED)
                .failureReason(failureReason)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducerService.sendPaymentFailedEvent(event);
        log.warn("Payment failed: {} - Reason: {}", payment.getPaymentNumber(), failureReason);
    }
}
