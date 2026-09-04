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
    ORDER_ADDRESS_QUERY_FAILED(5008, "Failed to query user address");

    private final int code;
    private final String message;
}
