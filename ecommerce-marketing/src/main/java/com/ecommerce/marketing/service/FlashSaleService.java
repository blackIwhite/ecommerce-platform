package com.ecommerce.marketing.service;

import com.ecommerce.api.marketing.dto.PromotionDTO.FlashSaleItemDTO;
import com.ecommerce.common.core.page.PageResult;

import java.util.List;

public interface FlashSaleService {

    PageResult<FlashSaleItemDTO> listItems(Long promotionId, int pageNum, int pageSize);

    FlashSaleItemDTO getItemDetail(Long itemId);

    List<FlashSaleItemDTO> getActiveItems(Long promotionId);

    void addItem(FlashSaleItemDTO dto);

    void updateItem(FlashSaleItemDTO dto);

    void deleteItem(Long itemId);

    boolean deductStock(Long itemId, int quantity);

    void restoreStock(Long itemId, int quantity);
}
