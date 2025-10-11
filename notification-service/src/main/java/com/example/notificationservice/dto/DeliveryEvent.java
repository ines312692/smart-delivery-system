package com.example.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {
    private String eventId;
    private String eventType;
    private Long deliveryId;
    private String deliveryNumber;
    private Long orderId;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String status;
    private String agentName;
    private String agentPhone;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String trackingUrl;
    private LocalDateTime timestamp;
}