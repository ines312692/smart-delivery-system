package com.example.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String eventId;
    private String eventType;
    private Long paymentId;
    private String paymentNumber;
    private Long orderId;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime timestamp;
}