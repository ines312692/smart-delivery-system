package com.example.paymentservice.repository;


import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByCustomerId(String customerId);
    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status = ?1 AND p.nextRetryAt <= ?2")
    List<Payment> findPendingRetries(PaymentStatus status, LocalDateTime now);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.retryCount < p.maxRetries")
    List<Payment> findRetryablePayments();
}