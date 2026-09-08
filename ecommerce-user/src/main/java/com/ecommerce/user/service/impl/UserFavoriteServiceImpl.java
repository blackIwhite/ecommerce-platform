package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.api.product.ProductApi;
import com.ecommerce.api.product.dto.SpuSimpleDTO;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.user.dto.UserFavoriteDTO;
import com.ecommerce.user.entity.UserFavorite;
import com.ecommerce.user.mapper.UserFavoriteMapper;
import com.ecommerce.user.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final ProductApi productApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long spuId) {
        Long userId = UserContextHolder.getUserId();
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getSpuId, spuId);
        UserFavorite existing = userFavoriteMapper.selectOne(wrapper);

        if (existing != null) {
            userFavoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            UserFavorite favorite = UserFavorite.builder()
                    .userId(userId)
                    .spuId(spuId)
                    .build();
            userFavoriteMapper.insert(favorite);
            return true;
        }
    }

    @Override
    public boolean isFavorite(Long spuId) {
        Long userId = UserContextHolder.getUserId();
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getSpuId, spuId);
        return userFavoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public PageResult<UserFavoriteDTO> listFavorites(int pageNum, int pageSize) {
        Long userId = UserContextHolder.getUserId();

        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .orderByDesc(UserFavorite::getCreateTime);

        List<UserFavorite> allFavorites = userFavoriteMapper.selectList(wrapper);
        int total = allFavorites.size();

        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, pageNum, pageSize);
        }

        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        if (fromIndex >= total) {
            return PageResult.of(Collections.emptyList(), (long) total, pageNum, pageSize);
        }
        List<UserFavorite> pageFavorites = allFavorites.subList(fromIndex, toIndex);

        List<Long> spuIds = pageFavorites.stream()
                .map(UserFavorite::getSpuId)
                .collect(Collectors.toList());

        Map<Long, UserFavorite> favMap = pageFavorites.stream()
                .collect(Collectors.toMap(UserFavorite::getSpuId, Function.identity()));

        List<UserFavoriteDTO> dtoList = Collections.emptyList();
        try {
            Result<List<SpuSimpleDTO>> result = productApi.getSpuListByIds(spuIds);
            if (result != null && result.getData() != null) {
                dtoList = result.getData().stream().map(spu -> {
                    UserFavorite fav = favMap.get(spu.getSpuId());
                    String firstImage = "";
                    if (spu.getImages() != null) {
                        try {
                            var arr = new com.fasterxml.jackson.databind.ObjectMapper()
                                    .readValue(spu.getImages(), List.class);
                            if (!arr.isEmpty()) firstImage = arr.get(0).toString();
                        } catch (Exception ignored) {}
                    }
                    return UserFavoriteDTO.builder()
                            .spuId(spu.getSpuId())
                            .spuName(spu.getName())
                            .image(firstImage)
                            .minPrice(spu.getMinPrice())
                            .createTime(fav != null ? fav.getCreateTime() : null)
                            .build();
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch SPU details for favorites", e);
        }

        return PageResult.of(dtoList, (long) total, pageNum, pageSize);
    }
}
