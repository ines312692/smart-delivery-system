package com.example.notificationservice.service;

import com.delivery.notification.model.Notification;
import com.delivery.notification.model.NotificationStatus;
import com.delivery.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryService {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        log.info("Checking for failed notifications to retry...");

        List<Notification> failedNotifications =
                notificationRepository.findRetryableNotifications();

        if (failedNotifications.isEmpty()) {
            log.info("No notifications to retry");
            return;
        }

        log.info("Found {} notifications to retry", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            if (notification.canRetry()) {
                try {
                    log.info("Retrying notification {} (Attempt {}/{})",
                            notification.getId(),
                            notification.getRetryCount() + 1,
                            notification.getMaxRetries());

                    notification.incrementRetryCount();
                    notification.setStatus(NotificationStatus.RETRY);
                    notificationRepository.save(notification);

                    // Retry sending
                    retryNotification(notification);

                } catch (Exception e) {
                    log.error("Error retrying notification: {}", notification.getId(), e);
                }
            }
        }
    }

    private void retryNotification(Notification notification) {
        // Re-trigger the notification send
        try {
            boolean sent = false;

            switch (notification.getNotificationType()) {
                case EMAIL:
                    // Retry email
                    sent = true; // Simulated
                    break;
                case SMS:
                    // Retry SMS
                    sent = true; // Simulated
                    break;
                case PUSH:
                    // Retry push
                    sent = true; // Simulated
                    break;
            }

            if (sent) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(java.time.LocalDateTime.now());
                log.info("Notification retry successful: {}", notification.getId());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                log.error("Notification retry failed: {}", notification.getId());
            }

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            log.error("Error in retry attempt", e);
        } finally {
            notificationRepository.save(notification);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void logNotificationStatistics() {
        Long pending = notificationRepository.countByStatus(NotificationStatus.PENDING);
        Long sent = notificationRepository.countByStatus(NotificationStatus.SENT);
        Long failed = notificationRepository.countByStatus(NotificationStatus.FAILED);

        log.info("Notification Statistics - Pending: {}, Sent: {}, Failed: {}",
                pending, sent, failed);
    }
}