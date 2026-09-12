package com.ecommerce.user.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.user.dto.DictItemDTO;
import com.ecommerce.user.dto.DictTypeDTO;

import java.util.List;

public interface DictService {

    PageResult<DictTypeDTO> pageTypes(int pageNum, int pageSize);

    List<DictTypeDTO> listAllTypes();

    Long createType(DictTypeDTO dto);

    void updateType(DictTypeDTO dto);

    void deleteType(Long id);

    List<DictItemDTO> listItems(String typeCode);

    Long createItem(DictItemDTO dto);

    void updateItem(DictItemDTO dto);

    void deleteItem(Long id);

    void updateItemStatus(Long id, Integer status);
}
