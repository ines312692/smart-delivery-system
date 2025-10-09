package com.example.paymentservice;




import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentMethod;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.service.KafkaProducerService;
import com.example.paymentservice.service.PaymentProcessorService;
import com.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private PaymentProcessorService paymentProcessorService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setPaymentNumber("PAY-123");
        payment.setOrderId(1L);
        payment.setOrderNumber("ORD-123");
        payment.setCustomerId("CUST-001");
        payment.setAmount(new BigDecimal("99.99"));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void testProcessPaymentAsync_UpdatesToProcessing() {
        // Arrange
        doNothing().when(paymentService).updatePaymentStatus(
                anyLong(), any(PaymentStatus.class), anyString(), anyString());
        doNothing().when(kafkaProducerService).sendPaymentCompletedEvent(any());

        // Act
        CompletableFuture<Void> result = paymentProcessorService.processPaymentAsync(payment);

        // Assert - Processing status should be set
        verify(paymentService, atLeastOnce()).updatePaymentStatus(
                eq(1L), eq(PaymentStatus.PROCESSING), isNull(), anyString());
    }
}