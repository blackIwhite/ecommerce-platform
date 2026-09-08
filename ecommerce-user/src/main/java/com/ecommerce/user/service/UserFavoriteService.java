package com.ecommerce.user.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.user.dto.UserFavoriteDTO;

public interface UserFavoriteService {

    boolean toggleFavorite(Long spuId);

    boolean isFavorite(Long spuId);

    PageResult<UserFavoriteDTO> listFavorites(int pageNum, int pageSize);
}
