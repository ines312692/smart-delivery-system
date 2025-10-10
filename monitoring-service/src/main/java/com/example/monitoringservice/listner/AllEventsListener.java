package com.example.monitoringservice.listner;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllEventsListener {

    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"order.created", "order.updated", "order.cancelled"},
            groupId = "monitoring-service-group"
    )
    public void handleOrderEvents(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            log.info("Received order event: {}", eventType);
            monitoringService.logEvent(eventType, "order-service", message, "RECEIVED");
        } catch (Exception e) {
            log.error("Error processing order event", e);
            monitoringService.logEvent("UNKNOWN", "order-service", message, "FAILED");
        }
    }

    @KafkaListener(
            topics = {"payment.completed", "payment.failed"},
            groupId = "monitoring-service-group"
    )
    public void handlePaymentEvents(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            log.info("Received payment event: {}", eventType);
            monitoringService.logEvent(eventType, "payment-service", message, "RECEIVED");
        } catch (Exception e) {
            log.error("Error processing payment event", e);
            monitoringService.logEvent("UNKNOWN", "payment-service", message, "FAILED");
        }
    }

    @KafkaListener(
            topics = {"delivery.assigned", "delivery.in-transit", "delivery.completed", "delivery.failed"},
            groupId = "monitoring-service-group"
    )
    public void handleDeliveryEvents(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            log.info("Received delivery event: {}", eventType);
            monitoringService.logEvent(eventType, "delivery-service", message, "RECEIVED");
        } catch (Exception e) {
            log.error("Error processing delivery event", e);
            monitoringService.logEvent("UNKNOWN", "delivery-service", message, "FAILED");
        }
    }

    @KafkaListener(
            topics = {"notification.sent", "notification.failed"},
            groupId = "monitoring-service-group"
    )
    public void handleNotificationEvents(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            log.info("Received notification event: {}", eventType);
            monitoringService.logEvent(eventType, "notification-service", message, "RECEIVED");
        } catch (Exception e) {
            log.error("Error processing notification event", e);
            monitoringService.logEvent("UNKNOWN", "notification-service", message, "FAILED");
        }
    }
}