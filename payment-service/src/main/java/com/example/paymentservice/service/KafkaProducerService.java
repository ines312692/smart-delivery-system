package com.example.paymentservice.service;


import com.example.paymentservice.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCompletedEvent(PaymentEvent event) {
        log.info("Publishing payment completed event: {}", event.getPaymentNumber());
        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, event.getPaymentNumber(), event);
    }

    public void sendPaymentFailedEvent(PaymentEvent event) {
        log.info("Publishing payment failed event: {}", event.getPaymentNumber());
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, event.getPaymentNumber(), event);
    }
}
