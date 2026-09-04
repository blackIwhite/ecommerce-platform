package com.ecommerce.product.controller;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.dto.BrandCreateRequest;
import com.ecommerce.product.dto.BrandDTO;
import com.ecommerce.product.dto.BrandPageRequest;
import com.ecommerce.product.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @RequireLogin
    @GetMapping("/admin/brand/page")
    public Result<PageResult<BrandDTO>> pageBrand(BrandPageRequest request) {
        return Result.success(brandService.pageBrand(request));
    }

    @GetMapping("/brand/list")
    public Result<List<BrandDTO>> listAllEnabled() {
        return Result.success(brandService.listAllEnabled());
    }

    @RequireLogin
    @PostMapping("/admin/brand")
    public Result<Long> createBrand(@Valid @RequestBody BrandCreateRequest request) {
        return Result.success(brandService.createBrand(request));
    }

    @RequireLogin
    @PutMapping("/admin/brand")
    public Result<Void> updateBrand(@Valid @RequestBody BrandCreateRequest request) {
        brandService.updateBrand(request);
        return Result.success();
    }

    @RequireLogin
    @DeleteMapping("/admin/brand/{id}")
    public Result<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return Result.success();
    }
}
