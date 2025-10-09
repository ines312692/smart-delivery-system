package com.example.paymentservice.dto;

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
    private BigDecimal amount;
    private com.example.paymentservice.model.PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime timestamp;
}