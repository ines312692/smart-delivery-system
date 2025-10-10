package com.example.monitoringservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_health")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serviceName;

    @Column(nullable = false)
    private String status; // UP, DOWN, DEGRADED

    private String healthUrl;

    private Integer responseTime;

    @Column(length = 1000)
    private String errorDetails;

    @UpdateTimestamp
    private LocalDateTime lastChecked;
}