package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentEvent;
import com.example.paymentservice.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCompleted(Payment payment) {
        PaymentEvent event = new PaymentEvent(payment.getOrderId(), PaymentStatus.COMPLETED);
        kafkaTemplate.send("payment.completed", event);
        log.info("Sent payment.completed for order {}", payment.getOrderId());
    }

    public void sendPaymentFailed(String orderId) {
        PaymentEvent event = new PaymentEvent(orderId, PaymentStatus.FAILED);
        kafkaTemplate.send("payment.failed", event);
        log.info("Sent payment.failed for order {}", orderId);
    }
}
