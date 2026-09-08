package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Aggregated order statistics for a date range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Total number of orders created in range. */
    private long orderCount;
    /** Sum of order total amount in range. */
    private BigDecimal totalAmount;
    /** Average order amount in range. */
    private BigDecimal avgAmount;
    /** Number of orders that have been paid (status 1-4). */
    private long paidCount;
    /** Number of orders refunded (cancelled after a successful payment). */
    private long refundedCount;
    /** Number of cancelled orders (status 5). */
    private long cancelCount;
}
