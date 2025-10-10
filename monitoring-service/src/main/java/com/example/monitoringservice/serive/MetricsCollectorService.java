package com.example.monitoringservice.serive;


import com.example.monitoringservice.repository.EventLogRepository;
import com.example.monitoringservice.repository.SystemMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsCollectorService {

    private final EventLogRepository eventLogRepository;
    private final SystemMetricsRepository systemMetricsRepository;
    private final WebSocketService webSocketService;

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void collectMetrics() {
        log.info("Collecting system metrics...");

        // Count events by type
        Long totalOrders = eventLogRepository.countByEventType("ORDER_CREATED");
        Long totalPayments = eventLogRepository.countByEventType("PAYMENT_COMPLETED") +
                eventLogRepository.countByEventType("PAYMENT_FAILED");
        Long successfulPayments = eventLogRepository.countByEventType("PAYMENT_COMPLETED");
        Long failedPayments = eventLogRepository.countByEventType("PAYMENT_FAILED");
        Long totalDeliveries = eventLogRepository.countByEventType("DELIVERY_COMPLETED") +
                eventLogRepository.countByEventType("DELIVERY_FAILED");
        Long completedDeliveries = eventLogRepository.countByEventType("DELIVERY_COMPLETED");
        Long totalNotifications = eventLogRepository.countByEventType("NOTIFICATION_SENT") +
                eventLogRepository.countByEventType("NOTIFICATION_FAILED");
        Long sentNotifications = eventLogRepository.countByEventType("NOTIFICATION_SENT");
        Long failedNotifications = eventLogRepository.countByEventType("NOTIFICATION_FAILED");

        // Calculate success rate
        Double paymentSuccessRate = totalPayments > 0 ?
                (successfulPayments.doubleValue() / totalPayments.doubleValue()) * 100 : 0.0;

        Long pendingDeliveries = totalDeliveries - completedDeliveries;

        // Save metrics
        SystemMetrics metrics = SystemMetrics.builder()
                .serviceName("smart-delivery-system")
                .totalOrders(totalOrders)
                .totalPayments(totalPayments)
                .totalDeliveries(totalDeliveries)
                .totalNotifications(totalNotifications)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .paymentSuccessRate(paymentSuccessRate)
                .completedDeliveries(completedDeliveries)
                .pendingDeliveries(pendingDeliveries)
                .sentNotifications(sentNotifications)
                .failedNotifications(failedNotifications)
                .build();

        SystemMetrics saved = systemMetricsRepository.save(metrics);
        log.info("Metrics collected: Orders={}, Payments={}, Success Rate={}%",
                totalOrders, totalPayments, String.format("%.2f", paymentSuccessRate));

        // Send real-time metrics update
        webSocketService.sendMetricsUpdate(saved);
    }

    public SystemMetrics getLatestMetrics() {
        return systemMetricsRepository.findTopByServiceNameOrderByCapturedAtDesc("smart-delivery-system")
                .orElse(new SystemMetrics());
    }
}