package com.example.paymentservice;


import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentProcessorService;
import com.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProcessorService paymentProcessorService;

    @InjectMocks
    private PaymentService paymentService;

    private OrderEvent orderEvent;
    private Payment payment;

    @BeforeEach
    void setUp() {
        orderEvent = OrderEvent.builder()
                .eventId("event-123")
                .eventType("ORDER_CREATED")
                .orderId(1L)
                .orderNumber("ORD-123")
                .customerId("CUST-001")
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .totalAmount(new BigDecimal("99.99"))
                .status("CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        payment = new Payment();
        payment.setId(1L);
        payment.setPaymentNumber("PAY-123");
        payment.setOrderId(1L);
        payment.setOrderNumber("ORD-123");
        payment.setCustomerId("CUST-001");
        payment.setAmount(new BigDecimal("99.99"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRetryCount(0);
        payment.setMaxRetries(3);
    }

    @Test
    void testInitiatePayment_Success() {
        // Arrange
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentProcessorService.processPaymentAsync(any(Payment.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<Void> result = paymentService.initiatePayment(orderEvent);

        // Assert
        assertNotNull(result);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentProcessorService, times(1)).processPaymentAsync(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_ToCompleted() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        paymentService.updatePaymentStatus(1L, PaymentStatus.COMPLETED, "TXN-123", "Success");

        // Assert
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getTransactionId()).isEqualTo("TXN-123");
        assertThat(payment.getCompletedAt()).isNotNull();
    }

    @Test
    void testUpdatePaymentStatus_ToFailed_SetsRetryTime() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        paymentService.updatePaymentStatus(1L, PaymentStatus.FAILED, null, "Gateway error");

        // Assert
        verify(paymentRepository, times(1)).save(any(Payment.class));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureReason()).isEqualTo("Gateway error");
        assertThat(payment.getNextRetryAt()).isNotNull();
    }

    @Test
    void testRetryFailedPayment() {
        // Arrange
        payment.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentProcessorService.processPaymentAsync(any(Payment.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        paymentService.retryFailedPayment(payment);

        // Assert
        assertThat(payment.getRetryCount()).isEqualTo(1);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        verify(paymentRepository, times(1)).save(payment);
        verify(paymentProcessorService, times(1)).processPaymentAsync(payment);
    }

    @Test
    void testCanRetry_WithinLimit() {
        // Arrange
        payment.setRetryCount(2);
        payment.setMaxRetries(3);
        payment.setStatus(PaymentStatus.PENDING);

        // Act & Assert
        assertTrue(payment.canRetry());
    }

    @Test
    void testCanRetry_ExceededLimit() {
        // Arrange
        payment.setRetryCount(3);
        payment.setMaxRetries(3);
        payment.setStatus(PaymentStatus.PENDING);

        // Act & Assert
        assertFalse(payment.canRetry());
    }
}



