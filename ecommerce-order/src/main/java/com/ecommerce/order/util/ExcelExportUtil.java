package com.ecommerce.order.util;

import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.order.dto.ProductSalesDTO;
import com.ecommerce.order.dto.SalesReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Excel (XLSX) export helpers for reporting data.
 *
 * <p>Every method builds an in-memory workbook and returns its raw bytes so that callers can
 * stream the result straight into an HTTP response.
 */
@Slf4j
public final class ExcelExportUtil {

    private static final String SALES_SHEET_NAME = "销售报表";
    private static final String PRODUCT_SALES_SHEET_NAME = "商品销量";

    /** Column width unit is 1/256 of a character width. */
    private static final int COLUMN_WIDTH = 20 * 256;

    private ExcelExportUtil() {
    }

    /**
     * Export a sales report as XLSX with columns: 日期, 订单数, 销售额.
     */
    public static byte[] exportSalesReport(List<SalesReportDTO> data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SALES_SHEET_NAME);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);

            writeHeader(sheet, headerStyle, "日期", "订单数", "销售额");

            int rowIndex = 1;
            if (data != null) {
                for (SalesReportDTO item : data) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(item.getDate() != null ? item.getDate() : "");
                    row.createCell(1).setCellValue(item.getOrderCount());
                    writeAmount(row.createCell(2), item.getTotalAmount(), amountStyle);
                }
            }
            setColumnWidths(sheet, 3);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Export sales report to excel failed: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "export sales report failed");
        }
    }

    /**
     * Export a product sales ranking as XLSX with columns: 商品ID, 商品名, 销量, 销售额.
     */
    public static byte[] exportProductSales(List<ProductSalesDTO> data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(PRODUCT_SALES_SHEET_NAME);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);
            CellStyle idStyle = createIdStyle(workbook);

            writeHeader(sheet, headerStyle, "商品ID", "商品名", "销量", "销售额");

            int rowIndex = 1;
            if (data != null) {
                for (ProductSalesDTO item : data) {
                    Row row = sheet.createRow(rowIndex++);
                    Cell idCell = row.createCell(0);
                    if (item.getSpuId() != null) {
                        idCell.setCellValue(item.getSpuId().doubleValue());
                        idCell.setCellStyle(idStyle);
                    } else {
                        idCell.setCellValue("");
                    }
                    row.createCell(1).setCellValue(item.getProductName() != null ? item.getProductName() : "");
                    row.createCell(2).setCellValue(item.getSalesCount());
                    writeAmount(row.createCell(3), item.getTotalAmount(), amountStyle);
                }
            }
            setColumnWidths(sheet, 4);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Export product sales to excel failed: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "export product sales failed");
        }
    }

    private static void writeHeader(Sheet sheet, CellStyle headerStyle, String... titles) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void writeAmount(Cell cell, BigDecimal amount, CellStyle amountStyle) {
        cell.setCellValue(amount != null ? amount.doubleValue() : 0D);
        cell.setCellStyle(amountStyle);
    }

    private static void setColumnWidths(Sheet sheet, int columnCount) {
        // Fixed widths keep the export independent of AWT font metrics in headless environments.
        for (int i = 0; i < columnCount; i++) {
            sheet.setColumnWidth(i, COLUMN_WIDTH);
        }
    }

    private static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static CellStyle createAmountStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private static CellStyle createIdStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0"));
        return style;
    }
}
