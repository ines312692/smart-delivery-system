package com.example.paymentservice.dto;

import com.example.paymentservice.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private String orderNumber;
    private String customerId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String cardNumber;
    private String cardHolderName;
    private String cvv;
    private String expiryDate;
}
