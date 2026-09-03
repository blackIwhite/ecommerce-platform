package com.ecommerce.common.mq.constant;

/**
 * RocketMQ topic and consumer group constants.
 */
public final class MqConstants {

    private MqConstants() {
    }

    // ---- Order topics ----

    public static final String ORDER_CREATE_TOPIC = "order-create-topic";
    public static final String ORDER_CANCEL_TOPIC = "order-cancel-topic";
    public static final String ORDER_PAY_SUCCESS_TOPIC = "order-pay-success-topic";
    public static final String ORDER_CLOSE_TOPIC = "order-close-topic";

    // ---- Inventory topics ----

    public static final String INVENTORY_LOCK_TOPIC = "inventory-lock-topic";
    public static final String INVENTORY_UNLOCK_TOPIC = "inventory-unlock-topic";
    public static final String INVENTORY_DEDUCT_TOPIC = "inventory-deduct-topic";

    // ---- Payment topics ----

    public static final String PAYMENT_SUCCESS_TOPIC = "payment-success-topic";
    public static final String PAYMENT_REFUND_TOPIC = "payment-refund-topic";

    // ---- Notification topics ----

    public static final String NOTIFY_ORDER_TOPIC = "notify-order-topic";

    // ---- Consumer groups ----

    public static final String ORDER_CONSUMER_GROUP = "order-consumer-group";
    public static final String INVENTORY_CONSUMER_GROUP = "inventory-consumer-group";
    public static final String PAYMENT_CONSUMER_GROUP = "payment-consumer-group";
    public static final String NOTIFY_CONSUMER_GROUP = "notify-consumer-group";

    // ---- Delay levels (RocketMQ built-in) ----

    /** 30 minutes delay for order auto-cancel. */
    public static final int DELAY_LEVEL_ORDER_CANCEL = 16;

    /** 5 minutes delay. */
    public static final int DELAY_LEVEL_5_MIN = 9;
}
