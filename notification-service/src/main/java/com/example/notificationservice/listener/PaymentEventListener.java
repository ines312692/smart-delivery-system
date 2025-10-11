package com.example.notificationservice.listener;

import com.delivery.notification.dto.PaymentEvent;
import com.delivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "payment.completed",
            groupId = "notification-service-group"
    )
    public void handlePaymentCompletedEvent(PaymentEvent event) {
        try {
            log.info("Received payment completed event: {}", event.getPaymentNumber());
            notificationService.sendPaymentSuccessNotification(event);
        } catch (Exception e) {
            log.error("Error processing payment completed event", e);
        }
    }

    @KafkaListener(
            topics = "payment.failed",
            groupId = "notification-service-group"
    )
    public void handlePaymentFailedEvent(PaymentEvent event) {
        try {
            log.info("Received payment failed event: {}", event.getPaymentNumber());
            notificationService.sendPaymentFailedNotification(event);
        } catch (Exception e) {
            log.error("Error processing payment failed event", e);
        }
    }
}
