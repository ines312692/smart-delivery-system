package com.example.paymentservice;

import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private OrderEvent orderEvent;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

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
    }

    @Test
    void testEndToEndPaymentFlow() throws Exception {
        // Act - Initiate payment
        CompletableFuture<Void> future = paymentService.initiatePayment(orderEvent);
        future.get(10, TimeUnit.SECONDS);

        // Wait for async processing
        Thread.sleep(3000);

        // Assert - Payment should be created and processed
        Optional<Payment> payments = paymentRepository.findByOrderId(1L);
        assertThat(payments).isNotEmpty();

        Payment payment = payments.get();
        assertThat(payment.getOrderNumber()).isEqualTo("ORD-123");
        assertThat(payment.getCustomerId()).isEqualTo("CUST-001");
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));

        // Status should be either COMPLETED or FAILED (due to random simulation)
        assertThat(payment.getStatus())
                .isIn(PaymentStatus.COMPLETED, PaymentStatus.FAILED, PaymentStatus.PROCESSING);
    }

    @Test
    void testPaymentStatusUpdate() {
        // Arrange - Create a payment
        Payment payment = new Payment();
        payment.setPaymentNumber("PAY-TEST-123");
        payment.setOrderId(1L);
        payment.setOrderNumber("ORD-123");
        payment.setCustomerId("CUST-001");
        payment.setAmount(new BigDecimal("99.99"));
        payment.setStatus(PaymentStatus.PENDING);
        Payment savedPayment = paymentRepository.save(payment);

        // Act - Update status
        paymentService.updatePaymentStatus(
                savedPayment.getId(),
                PaymentStatus.COMPLETED,
                "TXN-123",
                "Success"
        );

        // Assert
        Payment updatedPayment = paymentRepository.findById(savedPayment.getId()).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(updatedPayment.getTransactionId()).isEqualTo("TXN-123");
        assertThat(updatedPayment.getCompletedAt()).isNotNull();
    }

    @Test
    void testRetryMechanism() {
        // Arrange - Create a failed payment
        Payment payment = new Payment();
        payment.setPaymentNumber("PAY-TEST-456");
        payment.setOrderId(2L);
        payment.setOrderNumber("ORD-456");
        payment.setCustomerId("CUST-002");
        payment.setAmount(new BigDecimal("49.99"));
        payment.setStatus(PaymentStatus.FAILED);
        payment.setRetryCount(0);
        payment.setMaxRetries(3);
        Payment savedPayment = paymentRepository.save(payment);

        // Act - Retry payment
        paymentService.retryFailedPayment(savedPayment);

        // Assert
        Payment retriedPayment = paymentRepository.findById(savedPayment.getId()).orElseThrow();
        assertThat(retriedPayment.getRetryCount()).isEqualTo(1);
    }
}
