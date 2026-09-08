package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Product sales ranking row aggregated from order items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spuId;
    /** Product (SKU) name recorded on the order item at purchase time. */
    private String productName;
    /** Sum of purchased quantity. */
    private long salesCount;
    /** Sum of item total price. */
    private BigDecimal totalAmount;
}
