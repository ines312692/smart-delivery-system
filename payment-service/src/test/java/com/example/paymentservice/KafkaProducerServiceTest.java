package com.example.paymentservice;

import com.example.paymentservice.dto.PaymentEvent;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private PaymentEvent paymentEvent;

    @BeforeEach
    void setUp() {
        paymentEvent = PaymentEvent.builder()
                .eventId("event-123")
                .eventType("PAYMENT_COMPLETED")
                .paymentId(1L)
                .paymentNumber("PAY-123")
                .orderId(1L)
                .orderNumber("ORD-123")
                .customerId("CUST-001")
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-123")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void testSendPaymentCompletedEvent() {
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(null);

        // Act
        kafkaProducerService.sendPaymentCompletedEvent(paymentEvent);

        // Assert
        verify(kafkaTemplate, times(1))
                .send(eq("payment.completed"), eq("PAY-123"), eq(paymentEvent));
    }

    @Test
    void testSendPaymentFailedEvent() {
        // Arrange
        paymentEvent.setEventType("PAYMENT_FAILED");
        paymentEvent.setStatus(PaymentStatus.FAILED);
        paymentEvent.setFailureReason("Gateway error");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(null);

        // Act
        kafkaProducerService.sendPaymentFailedEvent(paymentEvent);

        // Assert
        verify(kafkaTemplate, times(1))
                .send(eq("payment.failed"), eq("PAY-123"), eq(paymentEvent));
    }
}
