package com.ecommerce.order.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.order.dto.OrderStatsDTO;
import com.ecommerce.order.dto.ProductSalesDTO;
import com.ecommerce.order.dto.SalesReportDTO;
import com.ecommerce.order.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/order/admin/report")
@RequiredArgsConstructor
@RequireLogin
@Tag(name = "报表与数据导出", description = "Report and data export APIs")
public class ReportController {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportService reportService;

    @GetMapping("/stats")
    public Result<OrderStatsDTO> getOrderStats(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getOrderStats(startDate, endDate));
    }

    @GetMapping("/sales")
    public Result<List<SalesReportDTO>> getSalesReport(@RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate,
                                                       @RequestParam(defaultValue = "day") String groupBy) {
        return Result.success(reportService.getSalesReport(startDate, endDate, groupBy));
    }

    @GetMapping("/products")
    public Result<List<ProductSalesDTO>> getProductSales(@RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getProductSalesReport(startDate, endDate));
    }

    @AuditLog(module = "报表", operation = "导出销售报表", description = "管理员导出销售报表 Excel")
    @GetMapping("/export/orders")
    public void exportOrders(@RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             HttpServletResponse response) throws IOException {
        // Build the workbook before touching the response so failures still return a JSON error.
        byte[] bytes = reportService.exportOrders(startDate, endDate);
        writeExcel(response, "orders.xlsx", bytes);
    }

    @AuditLog(module = "报表", operation = "导出商品销量", description = "管理员导出商品销量 Excel")
    @GetMapping("/export/products")
    public void exportProducts(@RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               HttpServletResponse response) throws IOException {
        byte[] bytes = reportService.exportProducts(startDate, endDate);
        writeExcel(response, "products.xlsx", bytes);
    }

    private void writeExcel(HttpServletResponse response, String fileName, byte[] bytes) throws IOException {
        response.setContentType(EXCEL_CONTENT_TYPE);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
