package com.ecommerce.marketing.controller;

import com.ecommerce.api.marketing.dto.CouponTemplateDTO;
import com.ecommerce.api.marketing.dto.UserCouponDTO;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.marketing.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @AuditLog(module = "营销", operation = "创建优惠券模板", description = "创建优惠券模板")
    @PostMapping("/admin/template")
    public Result<Void> createTemplate(@RequestBody @Valid CouponTemplateDTO dto) {
        couponService.createTemplate(dto);
        return Result.success();
    }

    @AuditLog(module = "营销", operation = "更新优惠券模板", description = "更新优惠券模板")
    @PutMapping("/admin/template")
    public Result<Void> updateTemplate(@RequestBody @Valid CouponTemplateDTO dto) {
        couponService.updateTemplate(dto);
        return Result.success();
    }

    @DeleteMapping("/admin/template/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        couponService.deleteTemplate(id);
        return Result.success();
    }

    @PutMapping("/admin/template/{id}/status")
    public Result<Void> updateTemplateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        couponService.updateTemplateStatus(id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/admin/template/list")
    public Result<PageResult<CouponTemplateDTO>> listTemplates(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(couponService.listTemplates(pageNum, pageSize, status));
    }

    @GetMapping("/admin/template/{id}")
    public Result<CouponTemplateDTO> getTemplate(@PathVariable Long id) {
        return Result.success(couponService.getTemplate(id));
    }

    @GetMapping("/template/available")
    public Result<List<CouponTemplateDTO>> listAvailableCoupons() {
        return Result.success(couponService.listAvailableCoupons());
    }

    @RequireLogin
    @PostMapping("/coupon/claim/{templateId}")
    public Result<Long> claimCoupon(@PathVariable Long templateId) {
        return Result.success(couponService.claimCoupon(templateId));
    }

    @RequireLogin
    @GetMapping("/coupon/my")
    public Result<List<UserCouponDTO>> getMyCoupons(@RequestParam(required = false) Integer status) {
        return Result.success(couponService.getMyCoupons(status));
    }
}
