package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderStatsDTO;
import com.ecommerce.order.dto.ProductSalesDTO;
import com.ecommerce.order.dto.SalesReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Read-only reporting queries over the order tables.
 *
 * <p>Date parameters are {@code yyyy-MM-dd} strings. The end date is inclusive: the range is
 * resolved as {@code create_time >= startDate AND create_time < endDate + 1 day} so that the
 * {@code idx_create_time} index can still be used.
 */
@Mapper
public interface ReportMapper {

    /**
     * Aggregate order statistics for the given date range.
     *
     * <p>Order status codes: 0=pending_pay, 1=paid, 2=pending_ship, 3=shipped, 4=completed,
     * 5=cancelled. A refunded order is a cancelled order that already had a successful payment
     * record ({@code t_payment.status = 1}).
     */
    @Select("""
            SELECT COUNT(*) AS order_count,
                   IFNULL(SUM(o.total_amount), 0) AS total_amount,
                   IFNULL(AVG(o.total_amount), 0) AS avg_amount,
                   IFNULL(SUM(CASE WHEN o.status IN (1, 2, 3, 4) THEN 1 ELSE 0 END), 0) AS paid_count,
                   IFNULL(SUM(CASE WHEN o.status = 5 AND EXISTS (
                       SELECT 1 FROM t_payment p
                       WHERE p.order_id = o.id AND p.status = 1 AND p.deleted = 0
                   ) THEN 1 ELSE 0 END), 0) AS refunded_count,
                   IFNULL(SUM(CASE WHEN o.status = 5 THEN 1 ELSE 0 END), 0) AS cancel_count
            FROM t_order o
            WHERE o.deleted = 0
              AND o.create_time >= #{startDate}
              AND o.create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY)
            """)
    OrderStatsDTO getOrderStats(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * Daily sales report, one row per day ordered by date ascending.
     */
    @Select("""
            SELECT DATE_FORMAT(o.create_time, '%Y-%m-%d') AS `date`,
                   COUNT(*) AS order_count,
                   IFNULL(SUM(o.total_amount), 0) AS total_amount
            FROM t_order o
            WHERE o.deleted = 0
              AND o.create_time >= #{startDate}
              AND o.create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY)
            GROUP BY DATE_FORMAT(o.create_time, '%Y-%m-%d')
            ORDER BY `date` ASC
            """)
    List<SalesReportDTO> getSalesByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * Monthly sales report, one row per month ordered by month ascending.
     */
    @Select("""
            SELECT DATE_FORMAT(o.create_time, '%Y-%m') AS `date`,
                   COUNT(*) AS order_count,
                   IFNULL(SUM(o.total_amount), 0) AS total_amount
            FROM t_order o
            WHERE o.deleted = 0
              AND o.create_time >= #{startDate}
              AND o.create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY)
            GROUP BY DATE_FORMAT(o.create_time, '%Y-%m')
            ORDER BY `date` ASC
            """)
    List<SalesReportDTO> getSalesByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * Top 10 products by sold quantity in the given date range.
     */
    @Select("""
            SELECT i.spu_id AS spu_id,
                   MAX(i.sku_name) AS product_name,
                   IFNULL(SUM(i.quantity), 0) AS sales_count,
                   IFNULL(SUM(i.total_price), 0) AS total_amount
            FROM t_order_item i
            INNER JOIN t_order o ON o.id = i.order_id AND o.deleted = 0
            WHERE i.deleted = 0
              AND i.spu_id IS NOT NULL
              AND o.create_time >= #{startDate}
              AND o.create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY)
            GROUP BY i.spu_id
            ORDER BY sales_count DESC
            LIMIT 10
            """)
    List<ProductSalesDTO> getProductSales(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
