package com.ecommerce.common.mq.constant;

public final class MqConstants {

    private MqConstants() {
    }

    // ---- Exchanges ----

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String NOTIFY_EXCHANGE = "notify.exchange";
    public static final String MARKETING_EXCHANGE = "marketing.exchange";
    public static final String AUDIT_EXCHANGE = "audit.exchange";

    // ---- Order queues ----

    public static final String ORDER_CREATE_QUEUE = "order.queue.create";
    public static final String ORDER_CANCEL_QUEUE = "order.queue.cancel";
    public static final String ORDER_PAY_SUCCESS_QUEUE = "order.queue.pay-success";
    public static final String ORDER_CLOSE_QUEUE = "order.queue.close";
    public static final String ORDER_CLOSE_DELAY_QUEUE = "order.queue.close.delay";

    // ---- Inventory queues ----

    public static final String INVENTORY_LOCK_QUEUE = "inventory.queue.lock";
    public static final String INVENTORY_UNLOCK_QUEUE = "inventory.queue.unlock";
    public static final String INVENTORY_DEDUCT_QUEUE = "inventory.queue.deduct";

    // ---- Marketing queues ----

    public static final String POINTS_EARN_QUEUE = "marketing.queue.points-earn";

    // ---- Audit queues ----

    public static final String AUDIT_QUEUE = "audit.queue";

    // ---- Routing keys ----

    public static final String ORDER_CREATE_KEY = "order.create";
    public static final String ORDER_CANCEL_KEY = "order.cancel";
    public static final String ORDER_PAY_SUCCESS_KEY = "order.pay-success";
    public static final String ORDER_CLOSE_KEY = "order.close";

    public static final String INVENTORY_LOCK_KEY = "inventory.lock";
    public static final String INVENTORY_UNLOCK_KEY = "inventory.unlock";
    public static final String INVENTORY_DEDUCT_KEY = "inventory.deduct";

    public static final String POINTS_EARN_KEY = "marketing.points-earn";

    public static final String AUDIT_ROUTING_KEY = "audit.log";

    // ---- Delay (milliseconds) ----

    public static final long DELAY_ORDER_CANCEL_MS = 30 * 60 * 1000L;
    public static final long DELAY_5_MIN_MS = 5 * 60 * 1000L;
}
