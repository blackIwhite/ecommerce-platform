package com.ecommerce.common.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order event message body for MQ communication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Order ID. */
    private Long orderId;

    /** User ID who placed the order. */
    private Long userId;

    /** SKU IDs in the order. */
    private List<Long> skuIds;

    /** Corresponding quantities for each SKU. */
    private List<Integer> quantities;

    /** Total order amount. */
    private BigDecimal totalAmount;

    /** Order status. */
    private Integer orderStatus;

    /** Reason for cancellation, if applicable. */
    private String cancelReason;

    /** Message timestamp. */
    private LocalDateTime timestamp;
}
