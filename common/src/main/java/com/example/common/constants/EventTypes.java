package com.delivery.common.constants;

public final class EventTypes {

    private EventTypes() {
        throw new IllegalStateException("Constants class");
    }

    // Order Event Types
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_UPDATED = "ORDER_UPDATED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";

    // Payment Event Types
    public static final String PAYMENT_INITIATED = "PAYMENT_INITIATED";
    public static final String PAYMENT_PROCESSING = "PAYMENT_PROCESSING";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_REFUNDED = "PAYMENT_REFUNDED";

    // Delivery Event Types
    public static final String DELIVERY_ASSIGNED = "DELIVERY_ASSIGNED";
    public static final String DELIVERY_PICKED_UP = "DELIVERY_PICKED_UP";
    public static final String DELIVERY_IN_TRANSIT = "DELIVERY_IN_TRANSIT";
    public static final String DELIVERY_ARRIVING = "DELIVERY_ARRIVING";
    public static final String DELIVERY_COMPLETED = "DELIVERY_COMPLETED";
    public static final String DELIVERY_FAILED = "DELIVERY_FAILED";

    // Notification Event Types
    public static final String NOTIFICATION_EMAIL = "NOTIFICATION_EMAIL";
    public static final String NOTIFICATION_SMS = "NOTIFICATION_SMS";
    public static final String NOTIFICATION_PUSH = "NOTIFICATION_PUSH";
}