package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.user.dto.UserDTO;
import com.ecommerce.user.dto.UserPageRequest;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RedisUtils redisUtils;

    private static final String USER_CACHE_KEY_PREFIX = "user:profile:";
    private static final long USER_CACHE_TTL_MINUTES = 30;

    @Override
    public UserDTO getUserById(Long userId) {
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        Object cached = redisUtils.get(cacheKey);
        if (cached instanceof UserDTO dto) {
            return dto;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserDTO dto = toUserDTO(user);
        redisUtils.set(cacheKey, dto, USER_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return dto;
    }

    @Override
    public void updateUser(UserUpdateRequest request) {
        Long currentUserId = UserContextHolder.getUserId();
        if (currentUserId != null && !currentUserId.equals(request.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Cannot update other user's profile");
        }

        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
        redisUtils.delete(USER_CACHE_KEY_PREFIX + request.getUserId());
    }

    @Override
    public PageResult<UserDTO> pageUsers(UserPageRequest request) {
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(User::getPhone, request.getKeyword())
                    .or().like(User::getNickname, request.getKeyword()));
        }
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(page, wrapper);
        List<UserDTO> dtoList = result.getRecords().stream()
                .map(this::toUserDTO)
                .toList();
        return PageResult.of(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .build();
    }
}
