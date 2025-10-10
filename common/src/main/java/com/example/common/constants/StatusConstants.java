package com.delivery.common.constants;

public final class StatusConstants {

    private StatusConstants() {
        throw new IllegalStateException("Constants class");
    }

    // Order Status
    public static final class OrderStatus {
        public static final String CREATED = "CREATED";
        public static final String PAYMENT_PENDING = "PAYMENT_PENDING";
        public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
        public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
        public static final String PREPARING = "PREPARING";
        public static final String READY_FOR_DELIVERY = "READY_FOR_DELIVERY";
        public static final String IN_DELIVERY = "IN_DELIVERY";
        public static final String DELIVERED = "DELIVERED";
        public static final String CANCELLED = "CANCELLED";
        public static final String REFUNDED = "REFUNDED";
    }

    // Payment Status
    public static final class PaymentStatus {
        public static final String PENDING = "PENDING";
        public static final String PROCESSING = "PROCESSING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
        public static final String REFUNDED = "REFUNDED";
        public static final String CANCELLED = "CANCELLED";
    }

    // Delivery Status
    public static final class DeliveryStatus {
        public static final String PENDING = "PENDING";
        public static final String ASSIGNED = "ASSIGNED";
        public static final String PICKED_UP = "PICKED_UP";
        public static final String IN_TRANSIT = "IN_TRANSIT";
        public static final String ARRIVING = "ARRIVING";
        public static final String DELIVERED = "DELIVERED";
        public static final String FAILED = "FAILED";
        public static final String CANCELLED = "CANCELLED";
    }

    // Notification Status
    public static final class NotificationStatus {
        public static final String PENDING = "PENDING";
        public static final String SENT = "SENT";
        public static final String FAILED = "FAILED";
        public static final String RETRY = "RETRY";
    }
}
