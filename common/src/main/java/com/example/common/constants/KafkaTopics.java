package com.delivery.common.constants;

public final class KafkaTopics {

    private KafkaTopics() {
        throw new IllegalStateException("Constants class");
    }

    // Order Topics
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_UPDATED = "order.updated";
    public static final String ORDER_CANCELLED = "order.cancelled";

    // Payment Topics
    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_PROCESSING = "payment.processing";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_REFUNDED = "payment.refunded";

    // Delivery Topics
    public static final String DELIVERY_ASSIGNED = "delivery.assigned";
    public static final String DELIVERY_PICKED_UP = "delivery.picked-up";
    public static final String DELIVERY_IN_TRANSIT = "delivery.in-transit";
    public static final String DELIVERY_ARRIVING = "delivery.arriving";
    public static final String DELIVERY_COMPLETED = "delivery.completed";
    public static final String DELIVERY_FAILED = "delivery.failed";

    // Notification Topics
    public static final String NOTIFICATION_SEND = "notification.send";
    public static final String NOTIFICATION_SENT = "notification.sent";
    public static final String NOTIFICATION_FAILED = "notification.failed";

    // Dead Letter Topics
    public static final String DLT_ORDER = "dlt.order";
    public static final String DLT_PAYMENT = "dlt.payment";
    public static final String DLT_DELIVERY = "dlt.delivery";
    public static final String DLT_NOTIFICATION = "dlt.notification";
}
