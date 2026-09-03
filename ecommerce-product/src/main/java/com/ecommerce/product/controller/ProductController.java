package com.ecommerce.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.dto.SpuCreateRequest;
import com.ecommerce.product.dto.SpuDTO;
import com.ecommerce.product.dto.SpuPageRequest;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.service.SkuService;
import com.ecommerce.product.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final SpuService spuService;
    private final SkuService skuService;

    @GetMapping("/spu/page")
    public Result<Page<SpuDTO>> pageSpu(SpuPageRequest request) {
        return Result.success(spuService.pageSpu(request));
    }

    @GetMapping("/spu/{spuId}")
    public Result<SpuDTO> getSpuDetail(@PathVariable Long spuId) {
        return Result.success(spuService.getSpuDetail(spuId));
    }

    @RequireLogin
    @PostMapping("/spu")
    public Result<Long> createSpu(@RequestBody SpuCreateRequest request) {
        return Result.success(spuService.createSpu(request));
    }

    @RequireLogin
    @PutMapping("/spu")
    public Result<Void> updateSpu(@RequestBody SpuCreateRequest request) {
        spuService.updateSpu(request);
        return Result.success();
    }

    @RequireLogin
    @PutMapping("/spu/{spuId}/status")
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
}
