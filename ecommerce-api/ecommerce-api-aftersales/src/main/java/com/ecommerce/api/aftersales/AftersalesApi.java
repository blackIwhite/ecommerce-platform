package com.ecommerce.api.aftersales;

import com.ecommerce.api.aftersales.dto.AftersalesOrderDTO;
import com.ecommerce.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ecommerce-aftersales")
public interface AftersalesApi {

    @GetMapping("/aftersales/internal/{id}")
    Result<AftersalesOrderDTO> getById(@PathVariable("id") Long id);
}
