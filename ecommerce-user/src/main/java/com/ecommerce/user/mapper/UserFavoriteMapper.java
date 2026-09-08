package com.ecommerce.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.user.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
