package com.example.monitoringservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_logs", indexes = {
        @Index(name = "idx_event_type", columnList = "eventType"),
        @Index(name = "idx_source_service", columnList = "sourceService"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String sourceService;

    @Column(length = 5000)
    private String payload;

    @Column(nullable = false)
    private String status; // RECEIVED, PROCESSED, FAILED

    @Column(length = 2000)
    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}
