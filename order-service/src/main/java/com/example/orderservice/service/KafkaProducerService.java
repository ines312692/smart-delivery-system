// ============================================
// 14. KafkaProducerService.java
// ============================================
package com.example.orderservice.service;

import com.example.orderservice.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private static final String ORDER_CREATED_TOPIC = "order.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderEvent event) {
        log.info("Publishing order created event to Kafka: {}", event.getOrderNumber());
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event.getOrderNumber(), event);
        log.info("Order created event published successfully");
    }
}