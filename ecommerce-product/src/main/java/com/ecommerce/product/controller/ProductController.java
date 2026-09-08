package com.ecommerce.product.controller;

import com.ecommerce.api.product.dto.SalesIncrementItem;
import com.ecommerce.api.product.dto.SpuSimpleDTO;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.dto.SpuCreateRequest;
import com.ecommerce.product.dto.SpuDTO;
import com.ecommerce.product.dto.SpuPageRequest;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.service.SkuService;
import com.ecommerce.product.service.SpuEsService;
import com.ecommerce.product.service.SpuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final SpuService spuService;
    private final SkuService skuService;
    private final SpuEsService spuEsService;

    @GetMapping("/spu/page")
    public Result<PageResult<SpuDTO>> pageSpu(SpuPageRequest request) {
        return Result.success(spuService.pageSpu(request));
    }

    @GetMapping("/spu/{spuId}")
    public Result<SpuDTO> getSpuDetail(@PathVariable Long spuId) {
        return Result.success(spuService.getSpuDetail(spuId));
    }

    @AuditLog(module = "商品", operation = "创建SPU", description = "创建商品SPU")
    @RequireLogin
    @PostMapping("/admin/spu")
    public Result<Long> createSpu(@RequestBody @Valid SpuCreateRequest request) {
        return Result.success(spuService.createSpu(request));
    }

    @AuditLog(module = "商品", operation = "更新SPU", description = "更新商品SPU")
    @RequireLogin
    @PutMapping("/admin/spu")
    public Result<Void> updateSpu(@RequestBody @Valid SpuCreateRequest request) {
        spuService.updateSpu(request);
        return Result.success();
    }

    @AuditLog(module = "商品", operation = "上下架", description = "修改商品状态")
    @RequireLogin
    @PutMapping("/admin/spu/{spuId}/status")
    public Result<Void> updateStatus(@PathVariable Long spuId, @RequestParam Integer status) {
        spuService.updateStatus(spuId, status);
        return Result.success();
    }

    @GetMapping("/sku/{skuId}")
    public Result<SkuDTO> getSkuById(@PathVariable Long skuId) {
        return Result.success(skuService.getSkuById(skuId));
    }

    @GetMapping("/sku/list")
    public Result<List<SkuDTO>> getSkuListByIds(@RequestParam List<Long> skuIds) {
        return Result.success(skuService.getSkuListByIds(skuIds));
    }

    @PostMapping("/admin/spu/sales/increment")
    public Result<Boolean> incrementSales(@RequestBody List<SalesIncrementItem> items) {
        spuService.incrementSales(items);
        return Result.success(true);
    }

    @GetMapping("/spu/list")
    public Result<List<SpuSimpleDTO>> getSpuListByIds(@RequestParam List<Long> spuIds) {
        return Result.success(spuService.getSpuSimpleList(spuIds));
    }

    @RequireLogin
    @PostMapping("/admin/spu/reindex")
    public Result<Void> reindexAll() {
        spuEsService.reindexAll();
        return Result.success();
    }
}
