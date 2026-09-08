package com.ecommerce.order.service.impl;

import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.order.dto.OrderStatsDTO;
import com.ecommerce.order.dto.ProductSalesDTO;
import com.ecommerce.order.dto.SalesReportDTO;
import com.ecommerce.order.mapper.ReportMapper;
import com.ecommerce.order.service.ReportService;
import com.ecommerce.order.util.ExcelExportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    private static final String GROUP_BY_DAY = "day";
    private static final String GROUP_BY_MONTH = "month";
    /** Window used when no start date is supplied. */
    private static final int DEFAULT_RANGE_DAYS = 30;

    @Override
    public OrderStatsDTO getOrderStats(String startDate, String endDate) {
        DateRange range = resolveRange(startDate, endDate);
        OrderStatsDTO stats = reportMapper.getOrderStats(range.start(), range.end());
        if (stats == null) {
            return OrderStatsDTO.builder()
                    .orderCount(0L)
                    .totalAmount(BigDecimal.ZERO)
                    .avgAmount(BigDecimal.ZERO)
                    .paidCount(0L)
                    .refundedCount(0L)
                    .cancelCount(0L)
                    .build();
        }
        if (stats.getTotalAmount() == null) {
            stats.setTotalAmount(BigDecimal.ZERO);
        }
        if (stats.getAvgAmount() == null) {
            stats.setAvgAmount(BigDecimal.ZERO);
        }
        return stats;
    }

    @Override
    public List<SalesReportDTO> getSalesReport(String startDate, String endDate, String groupBy) {
        DateRange range = resolveRange(startDate, endDate);
        if (GROUP_BY_MONTH.equalsIgnoreCase(StringUtils.hasText(groupBy) ? groupBy.trim() : GROUP_BY_DAY)) {
            return reportMapper.getSalesByMonth(range.start(), range.end());
        }
        return reportMapper.getSalesByDay(range.start(), range.end());
    }

    @Override
    public List<ProductSalesDTO> getProductSalesReport(String startDate, String endDate) {
        DateRange range = resolveRange(startDate, endDate);
        return reportMapper.getProductSales(range.start(), range.end());
    }

    @Override
    public byte[] exportOrders(String startDate, String endDate) {
        List<SalesReportDTO> data = getSalesReport(startDate, endDate, GROUP_BY_DAY);
        log.info("Exporting sales report to excel, rows={}", data != null ? data.size() : 0);
        return ExcelExportUtil.exportSalesReport(data);
    }

    @Override
    public byte[] exportProducts(String startDate, String endDate) {
        List<ProductSalesDTO> data = getProductSalesReport(startDate, endDate);
        log.info("Exporting product sales to excel, rows={}", data != null ? data.size() : 0);
        return ExcelExportUtil.exportProductSales(data);
    }

    /**
     * Validates the incoming dates and fills in defaults so the mapper always receives a complete,
     * well ordered {@code yyyy-MM-dd} range.
     */
    private DateRange resolveRange(String startDate, String endDate) {
        LocalDate end = StringUtils.hasText(endDate) ? parseDate(endDate.trim(), "endDate") : LocalDate.now();
        LocalDate start = StringUtils.hasText(startDate)
                ? parseDate(startDate.trim(), "startDate")
                : end.minusDays(DEFAULT_RANGE_DAYS - 1L);
        if (start.isAfter(end)) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "startDate must not be after endDate: " + start + " > " + end);
        }
        return new DateRange(start.toString(), end.toString());
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    fieldName + " must use yyyy-MM-dd format, got: " + value);
        }
    }

    private record DateRange(String start, String end) {
    }
}
