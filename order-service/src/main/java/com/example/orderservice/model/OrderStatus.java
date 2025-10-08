package com.delivery.order.model;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PREPARING,
    READY_FOR_DELIVERY,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED,
    REFUNDED
}