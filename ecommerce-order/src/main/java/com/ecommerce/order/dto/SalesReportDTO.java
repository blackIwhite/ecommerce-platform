package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Sales report row grouped by day (yyyy-MM-dd) or month (yyyy-MM).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Group key, e.g. 2026-09-01 for daily or 2026-09 for monthly reports. */
    private String date;
    private long orderCount;
    private BigDecimal totalAmount;
}
