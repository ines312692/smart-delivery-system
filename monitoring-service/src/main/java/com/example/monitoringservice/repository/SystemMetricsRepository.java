package com.example.monitoringservice.repository;


import com.example.monitoringservice.entity.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {

    @Query("SELECT s FROM SystemMetrics s ORDER BY s.capturedAt DESC")
    List<SystemMetrics> findLatestMetrics();

    Optional<SystemMetrics> findTopByServiceNameOrderByCapturedAtDesc(String serviceName);

    List<SystemMetrics> findByCapturedAtBetween(LocalDateTime start, LocalDateTime end);
}