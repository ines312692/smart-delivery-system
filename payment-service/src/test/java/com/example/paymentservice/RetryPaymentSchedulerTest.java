package com.example.paymentservice;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentService;
import com.example.paymentservice.service.RetryPaymentScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryPaymentSchedulerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private RetryPaymentScheduler retryPaymentScheduler;

    private Payment retryablePayment;

    @BeforeEach
    void setUp() {
        retryablePayment = new Payment();
        retryablePayment.setId(1L);
        retryablePayment.setPaymentNumber("PAY-123");
        retryablePayment.setAmount(new BigDecimal("99.99"));
        retryablePayment.setStatus(PaymentStatus.FAILED);
        retryablePayment.setRetryCount(1);
        retryablePayment.setMaxRetries(3);
        retryablePayment.setNextRetryAt(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void testRetryFailedPayments_WithRetryablePayments() {
        // Arrange
        List<Payment> payments = Arrays.asList(retryablePayment);
        when(paymentRepository.findPendingRetries(any(PaymentStatus.class), any(LocalDateTime.class)))
                .thenReturn(payments);
        doNothing().when(paymentService).retryFailedPayment(any(Payment.class));

        // Act
        retryPaymentScheduler.retryFailedPayments();

        // Assert
        verify(paymentRepository, times(1)).findPendingRetries(any(), any());
        verify(paymentService, times(1)).retryFailedPayment(retryablePayment);
    }

    @Test
    void testRetryFailedPayments_NoPaymentsToRetry() {
        // Arrange
        when(paymentRepository.findPendingRetries(any(PaymentStatus.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        retryPaymentScheduler.retryFailedPayments();

        // Assert
        verify(paymentRepository, times(1)).findPendingRetries(any(), any());
        verify(paymentService, never()).retryFailedPayment(any());
    }

    @Test
    void testRetryFailedPayments_SkipsNonRetryablePayments() {
        // Arrange
        retryablePayment.setRetryCount(3); // Exceeded max retries
        List<Payment> payments = Arrays.asList(retryablePayment);
        when(paymentRepository.findPendingRetries(any(PaymentStatus.class), any(LocalDateTime.class)))
                .thenReturn(payments);

        // Act
        retryPaymentScheduler.retryFailedPayments();

        // Assert
        verify(paymentRepository, times(1)).findPendingRetries(any(), any());
        verify(paymentService, never()).retryFailedPayment(any());
    }

    @Test
    void testLogPaymentStatistics() {
        // Arrange
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(Collections.emptyList());
        when(paymentRepository.findByStatus(PaymentStatus.PROCESSING)).thenReturn(Collections.emptyList());
        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(Collections.emptyList());
        when(paymentRepository.findByStatus(PaymentStatus.FAILED)).thenReturn(Collections.emptyList());

        // Act
        retryPaymentScheduler.logPaymentStatistics();

        // Assert
        verify(paymentRepository, times(4)).findByStatus(any(PaymentStatus.class));
    }
}
