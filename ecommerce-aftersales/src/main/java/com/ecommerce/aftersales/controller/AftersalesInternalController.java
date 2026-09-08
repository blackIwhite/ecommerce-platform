package com.ecommerce.aftersales.controller;

import com.ecommerce.aftersales.entity.AftersalesOrder;
import com.ecommerce.aftersales.mapper.AftersalesOrderMapper;
import com.ecommerce.api.aftersales.dto.AftersalesOrderDTO;
import com.ecommerce.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aftersales/internal")
@Tag(name = "售后内部接口", description = "Aftersales internal APIs")
@RequiredArgsConstructor
public class AftersalesInternalController {

    private final AftersalesOrderMapper aftersalesOrderMapper;

    @GetMapping("/{id}")
    public Result<AftersalesOrderDTO> getById(@PathVariable Long id) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null) {
            return Result.success(null);
        }
        AftersalesOrderDTO dto = AftersalesOrderDTO.builder()
                .id(order.getId())
                .aftersalesNo(order.getAftersalesNo())
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .type(order.getType())
                .status(order.getStatus())
                .reason(order.getReason())
                .refundAmount(order.getRefundAmount())
                .createTime(order.getCreateTime())
                .build();
        return Result.success(dto);
    }
}
