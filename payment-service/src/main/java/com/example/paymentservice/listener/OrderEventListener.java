package com.example.paymentservice.listener;

import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.service.PaymentProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentProcessorService paymentProcessorService;

    @KafkaListener(topics = "order.created", groupId = "payment-service-group")
    public void handleOrderCreated(OrderEvent event) {
        log.info("Received Order Event: {}", event);
        paymentProcessorService.processPayment(event);
    }
}
