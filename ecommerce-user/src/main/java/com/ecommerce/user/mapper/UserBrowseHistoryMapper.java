package com.ecommerce.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.user.entity.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {
}
