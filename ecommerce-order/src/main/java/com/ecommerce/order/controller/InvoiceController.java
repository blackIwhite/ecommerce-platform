package com.ecommerce.order.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.order.dto.InvoiceApplyRequest;
import com.ecommerce.order.dto.InvoiceDTO;
import com.ecommerce.order.service.InvoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "发票管理", description = "Invoice management APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/invoice/apply")
    public Result<Void> applyInvoice(@RequestBody @Valid InvoiceApplyRequest request,
                                     @RequestHeader("X-User-Id") Long userId) {
        invoiceService.applyInvoice(request, userId);
        return Result.success();
    }

    @GetMapping("/invoice/my")
    public Result<PageResult<InvoiceDTO>> listMyInvoices(@RequestHeader("X-User-Id") Long userId,
                                                         @RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(invoiceService.listUserInvoices(userId, pageNum, pageSize));
    }

    @GetMapping("/invoice/{id}")
    public Result<InvoiceDTO> getInvoice(@PathVariable Long id,
                                         @RequestHeader("X-User-Id") Long userId) {
        InvoiceDTO invoice = invoiceService.getInvoice(id);
        if (!userId.equals(invoice.getUserId())) {
            throw new BusinessException(ResultCode.INVOICE_ACCESS_DENIED);
        }
        return Result.success(invoice);
    }

    @GetMapping("/admin/invoice/list")
    public Result<PageResult<InvoiceDTO>> listAdminInvoices(@RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                            @RequestParam(required = false) Integer status) {
        return Result.success(invoiceService.listAdminInvoices(pageNum, pageSize, status));
    }

    @AuditLog(module = "发票", operation = "开票", description = "管理员开具发票")
    @PutMapping("/admin/invoice/{id}/issue")
    public Result<Void> issueInvoice(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        invoiceService.issueInvoice(id, body.get("invoiceNo"), body.get("invoiceUrl"));
        return Result.success();
    }

    @AuditLog(module = "发票", operation = "拒绝开票", description = "管理员拒绝开票申请")
    @PutMapping("/admin/invoice/{id}/reject")
    public Result<Void> rejectInvoice(@PathVariable Long id,
                                      @RequestBody Map<String, String> body) {
        invoiceService.rejectInvoice(id, body.get("reason"));
        return Result.success();
    }
}
