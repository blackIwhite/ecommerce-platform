package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.common.web.context.UserContext;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.user.dto.UserDTO;
import com.ecommerce.user.dto.UserPageRequest;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .phone("13800138000").password("hashed").nickname("TestUser").avatar("avatar.png").status(1).build();
        sampleUser.setId(1L);
        UserContextHolder.set(UserContext.builder().userId(1L).phone("13800138000").build());
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    // ---- getUserById ----

    @Test
    void getUserById_cacheHit_shouldReturnCached() {
        UserDTO cached = UserDTO.builder().userId(1L).nickname("Cached").build();
        when(redisUtils.get("user:profile:1")).thenReturn(cached);

        UserDTO result = userService.getUserById(1L);

        assertEquals("Cached", result.getNickname());
        verify(userMapper, never()).selectById(anyLong());
    }

    @Test
    void getUserById_cacheMiss_shouldQueryDbAndCache() {
        when(redisUtils.get("user:profile:1")).thenReturn(null);
        when(userMapper.selectById(1L)).thenReturn(sampleUser);

        UserDTO result = userService.getUserById(1L);

        assertEquals("TestUser", result.getNickname());
        verify(redisUtils).set(eq("user:profile:1"), any(UserDTO.class), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getUserById_notFound_shouldThrow() {
        when(redisUtils.get("user:profile:999")).thenReturn(null);
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getUserById(999L));
        assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    // ---- updateUser ----

    @Test
    void updateUser_success_shouldUpdateAndEvictCache() {
        when(userMapper.selectById(1L)).thenReturn(sampleUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UserUpdateRequest request = UserUpdateRequest.builder().userId(1L).nickname("NewName").build();
        userService.updateUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals("NewName", captor.getValue().getNickname());
        verify(redisUtils).delete("user:profile:1");
    }

    @Test
    void updateUser_notFound_shouldThrow() {
        UserContextHolder.clear();
        when(userMapper.selectById(999L)).thenReturn(null);

        UserUpdateRequest request = UserUpdateRequest.builder().userId(999L).nickname("X").build();
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateUser(request));
        assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateUser_ownershipMismatch_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(2L).build());

        UserUpdateRequest request = UserUpdateRequest.builder().userId(1L).nickname("X").build();
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateUser(request));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
    }

    // ---- pageUsers ----

    @Test
    void pageUsers_withKeywordAndStatus_shouldReturnFiltered() {
        UserPageRequest request = new UserPageRequest();
        request.setKeyword("138");
        request.setStatus(1);
        request.setPageNum(1);
        request.setPageSize(10);

        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleUser));
        page.setTotal(1);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<UserDTO> result = userService.pageUsers(request);

        assertEquals(1, result.getTotal());
        assertEquals("TestUser", result.getList().getFirst().getNickname());
    }

    @Test
    void pageUsers_emptyResult_shouldReturnEmpty() {
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        Page<User> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<UserDTO> result = userService.pageUsers(request);

        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
