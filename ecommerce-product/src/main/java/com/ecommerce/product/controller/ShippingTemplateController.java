package com.ecommerce.product.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.dto.ShippingFeeCalculateResult;
import com.ecommerce.product.dto.ShippingTemplateCreateRequest;
import com.ecommerce.product.dto.ShippingTemplateDTO;
import com.ecommerce.product.service.ShippingTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/product/shipping")
@RequiredArgsConstructor
@Tag(name = "运费模板", description = "Shipping template APIs")
public class ShippingTemplateController {

    private final ShippingTemplateService shippingTemplateService;

    @Operation(summary = "运费模板分页")
    @GetMapping("/list")
    public Result<PageResult<ShippingTemplateDTO>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(shippingTemplateService.page(status, pageNum, pageSize));
    }

    @Operation(summary = "运费模板详情")
    @GetMapping("/{id}")
    public Result<ShippingTemplateDTO> getDetail(@PathVariable Long id) {
        return Result.success(shippingTemplateService.getDetail(id));
    }

    @AuditLog(module = "运费", operation = "创建运费模板", description = "创建运费模板")
    @RequireLogin
    @PostMapping("/admin")
    public Result<Long> create(@RequestBody @Valid ShippingTemplateCreateRequest request) {
        return Result.success(shippingTemplateService.create(request));
    }

    @AuditLog(module = "运费", operation = "更新运费模板", description = "更新运费模板")
    @RequireLogin
    @PutMapping("/admin")
    public Result<Void> update(@RequestBody @Valid ShippingTemplateCreateRequest request) {
        shippingTemplateService.update(request);
        return Result.success();
    }

    @AuditLog(module = "运费", operation = "删除运费模板", description = "删除运费模板")
    @RequireLogin
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shippingTemplateService.delete(id);
        return Result.success();
    }

    @AuditLog(module = "运费", operation = "修改运费模板状态", description = "修改运费模板状态")
    @RequireLogin
    @PutMapping("/admin/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        shippingTemplateService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "计算运费")
    @GetMapping("/calculate")
    public Result<ShippingFeeCalculateResult> calculateFee(
            @RequestParam Long templateId,
            @RequestParam(required = false) String regionCode,
            @RequestParam BigDecimal quantity) {
        return Result.success(shippingTemplateService.calculateFee(templateId, regionCode, quantity));
    }
}
