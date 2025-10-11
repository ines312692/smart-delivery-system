package com.example.notificationservice.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_type", columnList = "notificationType"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_recipient", columnList = "recipient")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String recipient;

    private String subject;

    @Column(length = 5000)
    private String message;

    private String templateName;

    @Column(length = 2000)
    private String templateData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private String relatedEntityType; // ORDER, PAYMENT, DELIVERY

    private String relatedEntityId;

    @Column(length = 1000)
    private String errorMessage;

    private Integer retryCount = 0;

    private Integer maxRetries = 3;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime sentAt;

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetries &&
                this.status == NotificationStatus.FAILED;
    }
}