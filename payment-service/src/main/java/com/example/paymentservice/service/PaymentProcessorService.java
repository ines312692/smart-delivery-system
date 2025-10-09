package com.example.paymentservice.service;

import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorService {

    private final PaymentService paymentService;
    private final KafkaProducerService kafkaProducerService;

    @Async
    public void processPayment(OrderEvent event) {
        try {
            Payment payment = paymentService.createPayment(event);
            kafkaProducerService.sendPaymentCompleted(payment);
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", event.getOrderId(), e);
            kafkaProducerService.sendPaymentFailed(event.getOrderId());
        }
    }
}
