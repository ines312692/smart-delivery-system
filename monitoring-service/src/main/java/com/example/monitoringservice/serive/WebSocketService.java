package com.example.monitoringservice.serive;


import com.example.monitoringservice.entity.EventLog;
import com.example.monitoringservice.entity.ServiceHealth;
import com.example.monitoringservice.entity.SystemMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendEventUpdate(EventLog eventLog) {
        try {
            messagingTemplate.convertAndSend("/topic/events", eventLog);
            log.debug("Sent event update via WebSocket: {}", eventLog.getEventType());
        } catch (Exception e) {
            log.error("Error sending event update", e);
        }
    }

    public void sendMetricsUpdate(SystemMetrics metrics) {
        try {
            messagingTemplate.convertAndSend("/topic/metrics", metrics);
            log.debug("Sent metrics update via WebSocket");
        } catch (Exception e) {
            log.error("Error sending metrics update", e);
        }
    }

    public void sendHealthUpdate(ServiceHealth health) {
        try {
            messagingTemplate.convertAndSend("/topic/health", health);
            log.debug("Sent health update via WebSocket: {}", health.getServiceName());
        } catch (Exception e) {
            log.error("Error sending health update", e);
        }
    }

    public void sendAlert(String message) {
        try {
            Map<String, Object> alert = new HashMap<>();
            alert.put("message", message);
            alert.put("timestamp", LocalDateTime.now());
            alert.put("severity", "WARNING");

            messagingTemplate.convertAndSend("/topic/alerts", alert);
            log.info("Sent alert: {}", message);
        } catch (Exception e) {
            log.error("Error sending alert", e);
        }
    }
}