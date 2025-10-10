package com.example.monitoringservice.serive;


import com.example.monitoringservice.entity.EventLog;
import com.example.monitoringservice.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final EventLogRepository eventLogRepository;
    private final WebSocketService webSocketService;

    @Transactional
    public void logEvent(String eventType, String sourceService, String payload, String status) {
        EventLog eventLog = EventLog.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService(sourceService)
                .payload(payload)
                .status(status)
                .build();

        if ("PROCESSED".equals(status) || "FAILED".equals(status)) {
            eventLog.setProcessedAt(LocalDateTime.now());
        }

        EventLog saved = eventLogRepository.save(eventLog);
        log.debug("Event logged: {} from {}", eventType, sourceService);

        // Send real-time update via WebSocket
        webSocketService.sendEventUpdate(saved);
    }

    public Page<EventLog> getLatestEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventLogRepository.findLatestEvents(pageable);
    }

    public List<EventLog> getEventsByType(String eventType) {
        return eventLogRepository.findByEventType(eventType);
    }

    public List<EventLog> getEventsByService(String serviceService) {
        return eventLogRepository.findBySourceService(serviceService);
    }

    public Long countEventsByType(String eventType) {
        return eventLogRepository.countByEventType(eventType);
    }

    public Long countEventsByStatus(String status) {
        return eventLogRepository.countByStatus(status);
    }

    public List<Object[]> getEventStatistics() {
        return eventLogRepository.countEventsByType();
    }
}