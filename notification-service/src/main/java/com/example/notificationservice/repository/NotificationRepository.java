package com.example.notificationservice.repository;

import com.delivery.notification.model.Notification;
import com.delivery.notification.model.NotificationStatus;
import com.delivery.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient(String recipient);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByNotificationType(NotificationType type);

    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < n.maxRetries")
    List<Notification> findRetryableNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = ?1")
    Long countByStatus(NotificationStatus status);

    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
