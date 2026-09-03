package com.ecommerce.api.product;

import com.ecommerce.api.product.dto.SkuDTO;
import com.ecommerce.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ecommerce-product")
public interface ProductApi {

    @GetMapping("/product/sku/{skuId}")
    Result<SkuDTO> getSkuById(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/sku/list")
    Result<List<SkuDTO>> getSkuListByIds(@RequestParam("skuIds") List<Long> skuIds);
}
