package com.example.paymentservice.service;

import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment createPayment(OrderEvent event) {
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .amount(event.getTotalAmount())
                .status(PaymentStatus.COMPLETED)
                .method(PaymentMethod.CREDIT_CARD)
                .build();

        log.info("Saving payment for order {}", event.getOrderId());
        return paymentRepository.save(payment);
    }
}
