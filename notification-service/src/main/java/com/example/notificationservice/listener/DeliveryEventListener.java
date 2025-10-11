package com.example.notificationservice.listener;


import com.delivery.notification.dto.DeliveryEvent;
import com.delivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "delivery.assigned",
            groupId = "notification-service-group"
    )
    public void handleDeliveryAssignedEvent(DeliveryEvent event) {
        try {
            log.info("Received delivery assigned event: {}", event.getDeliveryNumber());
            notificationService.sendDeliveryAssignedNotification(event);
        } catch (Exception e) {
            log.error("Error processing delivery assigned event", e);
        }
    }

    @KafkaListener(
            topics = "delivery.in-transit",
            groupId = "notification-service-group"
    )
    public void handleDeliveryInTransitEvent(DeliveryEvent event) {
        try {
            log.info("Received delivery in-transit event: {}", event.getDeliveryNumber());
            notificationService.sendDeliveryInTransitNotification(event);
        } catch (Exception e) {
            log.error("Error processing delivery in-transit event", e);
        }
    }

    @KafkaListener(
            topics = "delivery.completed",
            groupId = "notification-service-group"
    )
    public void handleDeliveryCompletedEvent(DeliveryEvent event) {
        try {
            log.info("Received delivery completed event: {}", event.getDeliveryNumber());
            notificationService.sendDeliveryCompletedNotification(event);
        } catch (Exception e) {
            log.error("Error processing delivery completed event", e);
        }
    }
}
