package com.example.notificationservice.service;

import com.delivery.notification.dto.DeliveryEvent;
import com.delivery.notification.dto.OrderEvent;
import com.delivery.notification.dto.PaymentEvent;
import com.delivery.notification.model.Notification;
import com.delivery.notification.model.NotificationStatus;
import com.delivery.notification.model.NotificationType;
import com.delivery.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    // ORDER NOTIFICATIONS
    @Async("notificationExecutor")
    @Transactional
    public void sendOrderCreatedNotification(OrderEvent event) {
        log.info("Sending order created notification to: {}", event.getCustomerEmail());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", event.getCustomerName());
        templateData.put("orderNumber", event.getOrderNumber());
        templateData.put("totalAmount", event.getTotalAmount());

        createAndSendNotification(
                NotificationType.EMAIL,
                event.getCustomerEmail(),
                "Order Confirmation - " + event.getOrderNumber(),
                buildOrderCreatedMessage(event),
                "order-created",
                templateData,
                "ORDER",
                event.getOrderNumber()
        );
    }

    @Async("notificationExecutor")
    @Transactional
    public void sendOrderCancelledNotification(OrderEvent event) {
        log.info("Sending order cancelled notification to: {}", event.getCustomerEmail());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", event.getCustomerName());
        templateData.put("orderNumber", event.getOrderNumber());

        createAndSendNotification(
                NotificationType.EMAIL,
                event.getCustomerEmail(),
                "Order Cancelled - " + event.getOrderNumber(),
                buildOrderCancelledMessage(event),
                "order-cancelled",
                templateData,
                "ORDER",
                event.getOrderNumber()
        );
    }

    // PAYMENT NOTIFICATIONS
    @Async("notificationExecutor")
    @Transactional
    public void sendPaymentSuccessNotification(PaymentEvent event) {
        log.info("Sending payment success notification to: {}", event.getCustomerEmail());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", event.getCustomerName());
        templateData.put("orderNumber", event.getOrderNumber());
        templateData.put("paymentNumber", event.getPaymentNumber());
        templateData.put("amount", event.getAmount());
        templateData.put("transactionId", event.getTransactionId());

        // Send email
        createAndSendNotification(
                NotificationType.EMAIL,
                event.getCustomerEmail(),
                "Payment Successful - " + event.getPaymentNumber(),
                buildPaymentSuccessMessage(event),
                "payment-success",
                templateData,
                "PAYMENT",
                event.getPaymentNumber()
        );

        // Send SMS
        createAndSendNotification(
                NotificationType.SMS,
                event.getCustomerEmail(),
                null,
                "Your payment of $" + event.getAmount() + " for order " +
                        event.getOrderNumber() + " was successful!",
                null,
                null,
                "PAYMENT",
                event.getPaymentNumber()
        );
    }

    @Async("notificationExecutor")
    @Transactional
    public void sendPaymentFailedNotification(PaymentEvent event) {
        log.info("Sending payment failed notification to: {}", event.getCustomerEmail());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", event.getCustomerName());
        templateData.put("orderNumber", event.getOrderNumber());
        templateData.put("failureReason", event.getFailureReason());

        createAndSendNotification(
                NotificationType.EMAIL,
                event.getCustomerEmail(),
                "Payment Failed - " + event.getPaymentNumber(),
                buildPaymentFailedMessage(event),
                "payment-failed",
                templateData,
                "PAYMENT",
                event.getPaymentNumber()
        );
    }

    // DELIVERY NOTIFICATIONS
    @Async("notificationExecutor")
    @Transactional
    public void sendDeliveryAssignedNotification(DeliveryEvent event) {
        log.info("Sending delivery assigned notification");

        createAndSendNotification(
                NotificationType.SMS,
                event.getCustomerPhone(),
                null,
                "Your order " + event.getOrderNumber() + " is out for delivery! " +
                        "Agent: " + event.getAgentName() + ". Track: " + event.getTrackingUrl(),
                null,
                null,
                "DELIVERY",
                event.getDeliveryNumber()
        );
    }

    @Async("notificationExecutor")
    @Transactional
    public void sendDeliveryInTransitNotification(DeliveryEvent event) {
        log.info("Sending delivery in-transit notification");

        createAndSendNotification(
                NotificationType.PUSH,
                event.getCustomerPhone(),
                "Order In Transit",
                "Your order is on the way! ETA: " + event.getEstimatedDeliveryTime(),
                null,
                null,
                "DELIVERY",
                event.getDeliveryNumber()
        );
    }

    @Async("notificationExecutor")
    @Transactional
    public void sendDeliveryCompletedNotification(DeliveryEvent event) {
        log.info("Sending delivery completed notification");

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", event.getCustomerName());
        templateData.put("deliveryNumber", event.getDeliveryNumber());
        templateData.put("orderNumber", event.getOrderNumber());

        // Email
        createAndSendNotification(
                NotificationType.EMAIL,
                event.getCustomerPhone(),
                "Delivery Completed - " + event.getDeliveryNumber(),
                buildDeliveryCompletedMessage(event),
                "delivery-completed",
                templateData,
                "DELIVERY",
                event.getDeliveryNumber()
        );

        // SMS
        createAndSendNotification(
                NotificationType.SMS,
                event.getCustomerPhone(),
                null,
                "Your order " + event.getOrderNumber() + " has been delivered! Thank you!",
                null,
                null,
                "DELIVERY",
                event.getDeliveryNumber()
        );
    }

    // HELPER METHODS
    private void createAndSendNotification(
            NotificationType type,
            String recipient,
            String subject,
            String message,
            String templateName,
            Map<String, Object> templateData,
            String entityType,
            String entityId) {

        try {
            String templateDataJson = templateData != null ?
                    objectMapper.writeValueAsString(templateData) : null;

            Notification notification = Notification.builder()
                    .notificationType(type)
                    .recipient(recipient)
                    .subject(subject)
                    .message(message)
                    .templateName(templateName)
                    .templateData(templateDataJson)
                    .status(NotificationStatus.PENDING)
                    .relatedEntityType(entityType)
                    .relatedEntityId(entityId)
                    .retryCount(0)
                    .maxRetries(3)
                    .build();

            Notification saved = notificationRepository.save(notification);
            sendNotification(saved);

        } catch (Exception e) {
            log.error("Error creating notification", e);
        }
    }

    private void sendNotification(Notification notification) {
        try {
            notification.setStatus(NotificationStatus.SENDING);
            notificationRepository.save(notification);

            boolean sent = false;
            switch (notification.getNotificationType()) {
                case EMAIL:
                    sent = emailNotificationService.sendEmail(
                            notification.getRecipient(),
                            notification.getSubject(),
                            notification.getMessage()
                    );
                    break;
                case SMS:
                    sent = smsNotificationService.sendSms(
                            notification.getRecipient(),
                            notification.getMessage()
                    );
                    break;
                case PUSH:
                    sent = pushNotificationService.sendPushNotification(
                            notification.getRecipient(),
                            notification.getSubject(),
                            notification.getMessage()
                    );
                    break;
            }

            if (sent) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                log.info("Notification sent successfully: {}", notification.getId());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage("Failed to send notification");
            }

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            log.error("Error sending notification", e);
        } finally {
            notificationRepository.save(notification);
        }
    }

    // MESSAGE BUILDERS
    private String buildOrderCreatedMessage(OrderEvent event) {
        return String.format(
                "Dear %s,\n\n" +
                        "Thank you for your order!\n\n" +
                        "Order Number: %s\n" +
                        "Total Amount: $%.2f\n\n" +
                        "We are processing your order.\n\n" +
                        "Best regards,\nSmart Delivery Team",
                event.getCustomerName(),
                event.getOrderNumber(),
                event.getTotalAmount()
        );
    }

    private String buildOrderCancelledMessage(OrderEvent event) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your order %s has been cancelled.\n\n" +
                        "Best regards,\nSmart Delivery Team",
                event.getCustomerName(),
                event.getOrderNumber()
        );
    }

    private String buildPaymentSuccessMessage(PaymentEvent event) {
        return String.format(
                "Dear %s,\n\n" +
                        "Payment successful!\n\n" +
                        "Payment: %s\nOrder: %s\nAmount: $%.2f\nTransaction: %s\n\n" +
                        "Best regards,\nSmart Delivery Team",
                event.getCustomerName(),
                event.getPaymentNumber(),
                event.getOrderNumber(),
                event.getAmount(),
                event.getTransactionId()
        );
    }

    private String buildPaymentFailedMessage(PaymentEvent event) {
        return String.format(
                "Dear %s,\n\n" +
                        "Payment failed.\n\n" +
                        "Order: %s\nReason: %s\n\n" +
                        "Please try again.\n\n" +
                        "Best regards,\nSmart Delivery Team",
                event.getCustomerName(),
                event.getOrderNumber(),
                event.getFailureReason()
        );
    }

    private String buildDeliveryCompletedMessage(DeliveryEvent event) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your order has been delivered!\n\n" +
                        "Order: %s\nDelivery: %s\n\n" +
                        "Thank you!\n\n" +
                        "Best regards,\nSmart Delivery Team",
                event.getCustomerName(),
                event.getOrderNumber(),
                event.getDeliveryNumber()
        );
    }
}
