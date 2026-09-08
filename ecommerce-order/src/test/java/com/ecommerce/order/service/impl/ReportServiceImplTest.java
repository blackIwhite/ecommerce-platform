package com.ecommerce.order.service.impl;

import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.order.dto.OrderStatsDTO;
import com.ecommerce.order.dto.SalesReportDTO;
import com.ecommerce.order.mapper.ReportMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    // ---- getOrderStats ----

    @Test
    void getOrderStats_explicitRange_shouldDelegateResolvedDates() {
        OrderStatsDTO stats = OrderStatsDTO.builder()
                .orderCount(10L)
                .totalAmount(new BigDecimal("5000"))
                .avgAmount(new BigDecimal("500"))
                .paidCount(8L)
                .refundedCount(1L)
                .cancelCount(2L)
                .build();
        when(reportMapper.getOrderStats("2026-09-01", "2026-09-30")).thenReturn(stats);

        OrderStatsDTO result = reportService.getOrderStats("2026-09-01", "2026-09-30");

        assertSame(stats, result);
        assertEquals(10L, result.getOrderCount());
        assertEquals(1L, result.getRefundedCount());
        verify(reportMapper).getOrderStats("2026-09-01", "2026-09-30");
    }

    @Test
    void getOrderStats_noDates_shouldDefaultToLast30Days() {
        when(reportMapper.getOrderStats(anyString(), anyString())).thenReturn(null);

        reportService.getOrderStats(null, null);

        LocalDate today = LocalDate.now();
        verify(reportMapper).getOrderStats(today.minusDays(29).toString(), today.toString());
    }

    @Test
    void getOrderStats_nullMapperResult_shouldReturnZeroStats() {
        when(reportMapper.getOrderStats(anyString(), anyString())).thenReturn(null);

        OrderStatsDTO result = reportService.getOrderStats("2026-09-01", "2026-09-30");

        assertEquals(0L, result.getOrderCount());
        assertEquals(0L, result.getPaidCount());
        assertEquals(0L, result.getRefundedCount());
        assertEquals(0L, result.getCancelCount());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.getAvgAmount());
    }

    @Test
    void getOrderStats_nullAmounts_shouldDefaultToZero() {
        OrderStatsDTO stats = OrderStatsDTO.builder()
                .orderCount(3L)
                .totalAmount(null)
                .avgAmount(null)
                .build();
        when(reportMapper.getOrderStats(anyString(), anyString())).thenReturn(stats);

        OrderStatsDTO result = reportService.getOrderStats("2026-09-01", "2026-09-30");

        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.getAvgAmount());
    }

    @Test
    void getOrderStats_invalidDateFormat_shouldThrowParamError() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> reportService.getOrderStats("2026/09/01", "2026-09-30"));

        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("startDate"));
        verifyNoInteractions(reportMapper);
    }

    @Test
    void getOrderStats_startAfterEnd_shouldThrowParamError() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> reportService.getOrderStats("2026-09-30", "2026-09-01"));

        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verifyNoInteractions(reportMapper);
    }

    // ---- getSalesReport ----

    @Test
    void getSalesReport_groupByMonth_shouldUseMonthlyQuery() {
        List<SalesReportDTO> rows = List.of(
                SalesReportDTO.builder().date("2026-09").orderCount(4L).totalAmount(new BigDecimal("800")).build());
        when(reportMapper.getSalesByMonth("2026-09-01", "2026-09-30")).thenReturn(rows);

        List<SalesReportDTO> result = reportService.getSalesReport("2026-09-01", "2026-09-30", " MONTH ");

        assertEquals(rows, result);
        verify(reportMapper, never()).getSalesByDay(anyString(), anyString());
    }

    @Test
    void getSalesReport_defaultGroupBy_shouldUseDailyQuery() {
        List<SalesReportDTO> rows = List.of(
                SalesReportDTO.builder().date("2026-09-01").orderCount(2L).totalAmount(new BigDecimal("300")).build());
        when(reportMapper.getSalesByDay("2026-09-01", "2026-09-30")).thenReturn(rows);

        List<SalesReportDTO> result = reportService.getSalesReport("2026-09-01", "2026-09-30", null);

        assertEquals(rows, result);
        verify(reportMapper, never()).getSalesByMonth(anyString(), anyString());
    }

    // ---- getProductSalesReport ----

    @Test
    void getProductSalesReport_onlyEndDate_shouldDeriveStartFromDefaultWindow() {
        when(reportMapper.getProductSales(anyString(), anyString())).thenReturn(List.of());

        reportService.getProductSalesReport(null, "2026-09-30");

        verify(reportMapper).getProductSales(
                LocalDate.parse("2026-09-30").minusDays(29).toString(), "2026-09-30");
    }
}
