package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderStatsDTO;
import com.ecommerce.order.dto.ProductSalesDTO;
import com.ecommerce.order.dto.SalesReportDTO;

import java.util.List;

/**
 * Reporting and data export service.
 *
 * <p>All date parameters use the {@code yyyy-MM-dd} format and both bounds are inclusive. Blank
 * bounds are resolved to a default range (last 30 days) by the implementation.
 */
public interface ReportService {

    /**
     * Aggregated order statistics for the given date range.
     */
    OrderStatsDTO getOrderStats(String startDate, String endDate);

    /**
     * Sales report grouped by day or month.
     *
     * @param groupBy {@code day} (default) or {@code month}
     */
    List<SalesReportDTO> getSalesReport(String startDate, String endDate, String groupBy);

    /**
     * Top 10 products by sold quantity for the given date range.
     */
    List<ProductSalesDTO> getProductSalesReport(String startDate, String endDate);

    /**
     * Export the daily sales report as XLSX bytes.
     */
    byte[] exportOrders(String startDate, String endDate);

    /**
     * Export the product sales ranking as XLSX bytes.
     */
    byte[] exportProducts(String startDate, String endDate);
}
