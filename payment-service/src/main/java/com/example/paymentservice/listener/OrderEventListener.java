package com.example.paymentservice.listener;

import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order.created",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(OrderEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received order created event: {}", event.getOrderNumber());

            paymentService.initiatePayment(event);

            acknowledgment.acknowledge();
            log.info("Successfully processed order event: {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing order event: {}", event.getOrderNumber(), e);
            // Don't acknowledge - message will be redelivered
        }
    }
}

