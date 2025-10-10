package com.example.monitoringservice.serive;


import com.example.monitoringservice.repository.ServiceHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final ServiceHealthRepository serviceHealthRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final WebSocketService webSocketService;

    private final Map<String, String> serviceUrls = new HashMap<>() {{
        put("order-service", "http://localhost:8081/actuator/health");
        put("payment-service", "http://localhost:8082/actuator/health");
        put("delivery-service", "http://localhost:8083/actuator/health");
        put("notification-service", "http://localhost:8084/actuator/health");
    }};

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @Transactional
    public void checkAllServices() {
        log.info("Checking health of all services...");

        for (Map.Entry<String, String> entry : serviceUrls.entrySet()) {
            checkServiceHealth(entry.getKey(), entry.getValue());
        }
    }

    private void checkServiceHealth(String serviceName, String healthUrl) {
        ServiceHealth health = serviceHealthRepository.findByServiceName(serviceName)
                .orElse(ServiceHealth.builder()
                        .serviceName(serviceName)
                        .healthUrl(healthUrl)
                        .build());

        try {
            long startTime = System.currentTimeMillis();
            Map<String, Object> response = restTemplate.getForObject(healthUrl, Map.class);
            long responseTime = (int) (System.currentTimeMillis() - startTime);

            String status = response != null && "UP".equals(response.get("status")) ? "UP" : "DOWN";

            health.setStatus(status);
            health.setResponseTime((int) responseTime);
            health.setErrorDetails(null);
            health.setLastChecked(LocalDateTime.now());

            log.info("Service {} is {} ({}ms)", serviceName, status, responseTime);

        } catch (Exception e) {
            health.setStatus("DOWN");
            health.setErrorDetails(e.getMessage());
            health.setLastChecked(LocalDateTime.now());
            log.error("Service {} is DOWN: {}", serviceName, e.getMessage());
        }

        ServiceHealth saved = serviceHealthRepository.save(health);
        webSocketService.sendHealthUpdate(saved);
    }

    public List<ServiceHealth> getAllServicesHealth() {
        return serviceHealthRepository.findAllByOrderByServiceName();
    }

    public ServiceHealth getServiceHealth(String serviceName) {
        return serviceHealthRepository.findByServiceName(serviceName)
                .orElse(null);
    }
}