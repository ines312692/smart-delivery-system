package com.example.monitoringservice.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serviceName;

    private Long totalOrders;
    private Long totalPayments;
    private Long totalDeliveries;
    private Long totalNotifications;

    private Long successfulPayments;
    private Long failedPayments;
    private Double paymentSuccessRate;

    private Long completedDeliveries;
    private Long pendingDeliveries;

    private Long sentNotifications;
    private Long failedNotifications;

    @CreationTimestamp
    private LocalDateTime capturedAt;
}