package com.example.notificationservice.listener;


import com.delivery.notification.dto.OrderEvent;
import com.delivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order.created",
            groupId = "notification-service-group"
    )
    public void handleOrderCreatedEvent(OrderEvent event) {
        try {
            log.info("Received order created event: {}", event.getOrderNumber());
            notificationService.sendOrderCreatedNotification(event);
        } catch (Exception e) {
            log.error("Error processing order created event", e);
        }
    }

    @KafkaListener(
            topics = "order.cancelled",
            groupId = "notification-service-group"
    )
    public void handleOrderCancelledEvent(OrderEvent event) {
        try {
            log.info("Received order cancelled event: {}", event.getOrderNumber());
            notificationService.sendOrderCancelledNotification(event);
        } catch (Exception e) {
            log.error("Error processing order cancelled event", e);
        }
    }
}