package com.ecommerce.aftersales.controller;

import com.ecommerce.aftersales.service.AftersalesService;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/aftersales/admin")
@RequiredArgsConstructor
public class AftersalesAdminController {

    private final AftersalesService aftersalesService;

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(aftersalesService.listForAdmin(type, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(aftersalesService.getAdminDetail(id));
    }

    @AuditLog(module = "售后", operation = "审核通过", description = "管理员审核通过售后申请")
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String handler = body != null ? body.getOrDefault("handler", "admin") : "admin";
        aftersalesService.approve(id, handler);
        return Result.success();
    }

    @AuditLog(module = "售后", operation = "审核拒绝", description = "管理员审核拒绝售后申请")
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String handler = body.getOrDefault("handler", "admin");
        String remark = body.getOrDefault("remark", "");
        aftersalesService.reject(id, handler, remark);
        return Result.success();
    }

    @PutMapping("/{id}/receive")
    public Result<Void> confirmReceive(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String handler = body != null ? body.getOrDefault("handler", "admin") : "admin";
        aftersalesService.confirmReceive(id, handler);
        return Result.success();
    }

    @PutMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String handler = body != null ? body.getOrDefault("handler", "admin") : "admin";
        aftersalesService.refund(id, handler);
        return Result.success();
    }

    @PutMapping("/{id}/ship-exchange")
    public Result<Void> shipExchange(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String handler = body.getOrDefault("handler", "admin");
        String trackingNo = body.get("trackingNo");
        String company = body.get("company");
        aftersalesService.shipExchange(id, handler, trackingNo, company);
        return Result.success();
    }
}
