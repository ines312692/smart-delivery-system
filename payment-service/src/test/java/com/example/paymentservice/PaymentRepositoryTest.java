package com.example.paymentservice;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentMethod;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        payment = new Payment();
        payment.setPaymentNumber("PAY-TEST-123");
        payment.setOrderId(1L);
        payment.setOrderNumber("ORD-123");
        payment.setCustomerId("CUST-001");
        payment.setAmount(new BigDecimal("99.99"));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRetryCount(0);
        payment.setMaxRetries(3);
    }

    @Test
    void testSavePayment() {
        // Act
        Payment savedPayment = paymentRepository.save(payment);

        // Assert
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getPaymentNumber()).isEqualTo("PAY-TEST-123");
        assertThat(savedPayment.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindByPaymentNumber() {
        // Arrange
        paymentRepository.save(payment);

        // Act
        Optional<Payment> found = paymentRepository.findByPaymentNumber("PAY-TEST-123");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-123");
    }

    @Test
    void testFindByOrderId() {
        // Arrange
        paymentRepository.save(payment);

        // Act
        Optional<Payment> found = paymentRepository.findByOrderId(1L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getPaymentNumber()).isEqualTo("PAY-TEST-123");
    }

    @Test
    void testFindByCustomerId() {
        // Arrange
        paymentRepository.save(payment);

        Payment payment2 = new Payment();
        payment2.setPaymentNumber("PAY-TEST-456");
        payment2.setOrderId(2L);
        payment2.setOrderNumber("ORD-456");
        payment2.setCustomerId("CUST-001");
        payment2.setAmount(new BigDecimal("49.99"));
        payment2.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment2.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment2);

        // Act
        List<Payment> payments = paymentRepository.findByCustomerId("CUST-001");

        // Assert
        assertThat(payments).hasSize(2);
    }

    @Test
    void testFindByStatus() {
        // Arrange
        paymentRepository.save(payment);

        Payment completedPayment = new Payment();
        completedPayment.setPaymentNumber("PAY-TEST-456");
        completedPayment.setOrderId(2L);
        completedPayment.setOrderNumber("ORD-456");
        completedPayment.setCustomerId("CUST-002");
        completedPayment.setAmount(new BigDecimal("49.99"));
        completedPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        completedPayment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(completedPayment);

        // Act
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        // Assert
        assertThat(pendingPayments).hasSize(1);
        assertThat(pendingPayments.get(0).getPaymentNumber()).isEqualTo("PAY-TEST-123");
    }

    @Test
    void testFindPendingRetries() {
        // Arrange
        payment.setStatus(PaymentStatus.FAILED);
        payment.setNextRetryAt(LocalDateTime.now().minusMinutes(5));
        paymentRepository.save(payment);

        // Act
        List<Payment> pendingRetries = paymentRepository.findPendingRetries(
                PaymentStatus.FAILED, LocalDateTime.now());

        // Assert
        assertThat(pendingRetries).hasSize(1);
        assertThat(pendingRetries.get(0).getPaymentNumber()).isEqualTo("PAY-TEST-123");
    }

    @Test
    void testFindRetryablePayments() {
        // Arrange
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRetryCount(1);
        paymentRepository.save(payment);

        // Act
        List<Payment> retryablePayments = paymentRepository.findRetryablePayments();

        // Assert
        assertThat(retryablePayments).hasSize(1);
    }
}
