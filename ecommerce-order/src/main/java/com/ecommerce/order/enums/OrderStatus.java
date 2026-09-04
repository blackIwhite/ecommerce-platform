package com.ecommerce.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PENDING_PAY(0),
    PAID(1),
    PENDING_SHIP(2),
    SHIPPED(3),
    COMPLETED(4),
    CANCELLED(5);

    private final int code;

    public static OrderStatus from(Integer code) {
        if (code == null) return null;
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING_PAY -> target == PAID || target == CANCELLED;
            case PAID -> target == PENDING_SHIP;
            case PENDING_SHIP -> target == SHIPPED;
            case SHIPPED -> target == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
