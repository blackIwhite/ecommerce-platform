package com.ecommerce.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Common business result codes.
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "Success"),
    FAIL(500, "Operation failed"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error"),

    // Common business codes (1xxx)
    PARAM_ERROR(1001, "Parameter validation error"),

    // User module codes (2xxx)
    USER_NOT_FOUND(2001, "User not found"),
    USER_ALREADY_EXISTS(2002, "User already exists"),
    ADDRESS_NOT_FOUND(2003, "Address not found"),

    // Product module codes (3xxx)
    PRODUCT_NOT_FOUND(3001, "Product not found"),
    PRODUCT_OFF_SHELF(3002, "Product is off shelf"),
    PRODUCT_ID_REQUIRED(3003, "Product id is required for update"),

    // Inventory module codes (4xxx)
    INVENTORY_NOT_ENOUGH(4001, "Inventory not enough"),
    INVENTORY_NOT_FOUND(4002, "Inventory record not found"),
    INVENTORY_LOCK_FAILED(4003, "Failed to acquire inventory lock"),
    INVENTORY_ADJUST_INVALID(4004, "Stock adjustment would result in negative stock"),

    // Order module codes (5xxx)
    ORDER_NOT_FOUND(5001, "Order not found"),
    ORDER_STATUS_ERROR(5002, "Order status error"),
    ORDER_ACCESS_DENIED(5003, "No permission to operate this order"),
    ORDER_INVENTORY_LOCK_FAILED(5004, "Failed to lock inventory"),
    ORDER_INVENTORY_UNLOCK_FAILED(5005, "Failed to unlock inventory"),
    ORDER_INVENTORY_DEDUCT_FAILED(5006, "Failed to deduct inventory"),
    ORDER_PRODUCT_QUERY_FAILED(5007, "Failed to query product info"),
    ORDER_ADDRESS_QUERY_FAILED(5008, "Failed to query user address"),

    // Review module codes (3xxx continued)
    REVIEW_ALREADY_EXISTS(3004, "This order product has already been reviewed"),
    REVIEW_ORDER_NOT_COMPLETED(3005, "Can only review after order is completed"),
    REVIEW_ORDER_ACCESS_DENIED(3006, "No permission to review this order"),

    // Marketing module codes (6xxx)
    COUPON_TEMPLATE_NOT_FOUND(6001, "Coupon template not found"),
    COUPON_TEMPLATE_DISABLED(6002, "Coupon template is disabled"),
    COUPON_CLAIM_LIMIT_EXCEEDED(6003, "Coupon claim limit exceeded"),
    COUPON_OUT_OF_STOCK(6004, "Coupon is out of stock"),
    COUPON_NOT_AVAILABLE(6005, "Coupon is not available"),
    USER_COUPON_NOT_FOUND(6006, "User coupon not found"),
    USER_COUPON_ALREADY_USED(6007, "User coupon is already used"),
    USER_COUPON_EXPIRED(6008, "User coupon has expired"),
    COUPON_NOT_USABLE(6009, "Coupon does not meet usage conditions"),
    POINTS_ACCOUNT_NOT_FOUND(6010, "Points account not found"),
    POINTS_INSUFFICIENT(6011, "Insufficient points balance"),
    POINTS_EARN_FAILED(6012, "Failed to earn points"),
    POINTS_REDEEM_FAILED(6013, "Failed to redeem points"),
    POINTS_RULE_NOT_FOUND(6014, "Points rule not found"),

    // Aftersales module codes (7xxx)
    AFTERSALES_NOT_FOUND(7001, "Aftersales order not found"),
    AFTERSALES_ORDER_NOT_PAID(7002, "Order must be paid before applying for aftersales"),
    AFTERSALES_STATUS_ERROR(7003, "Aftersales status does not allow this operation");

    private final int code;
    private final String message;
}
