package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.web.context.UserContext;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.user.dto.UserAddressCreateRequest;
import com.ecommerce.user.dto.UserAddressDTO;
import com.ecommerce.user.dto.UserAddressUpdateRequest;
import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.mapper.UserAddressMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceImplTest {

    @Mock
    private UserAddressMapper userAddressMapper;

    @InjectMocks
    private UserAddressServiceImpl userAddressService;

    private UserAddress sampleAddress;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), UserAddress.class);

        sampleAddress = UserAddress.builder()
                .userId(1L).receiverName("张三").receiverPhone("13800138000")
                .province("广东省").city("深圳市").district("南山区")
                .detailAddress("科技园路1号").isDefault(1).build();
        sampleAddress.setId(10L);
        UserContextHolder.set(UserContext.builder().userId(1L).build());
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    // ---- getAddressList ----

    @Test
    void getAddressList_shouldReturnMappedList() {
        when(userAddressMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleAddress));

        List<UserAddressDTO> result = userAddressService.getAddressList(1L);

        assertEquals(1, result.size());
        assertEquals("张三", result.getFirst().getReceiverName());
    }

    // ---- getAddressById ----

    @Test
    void getAddressById_found_shouldReturn() {
        when(userAddressMapper.selectById(10L)).thenReturn(sampleAddress);

        UserAddressDTO result = userAddressService.getAddressById(10L);

        assertEquals("张三", result.getReceiverName());
    }

    @Test
    void getAddressById_notFound_shouldThrow() {
        when(userAddressMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAddressService.getAddressById(999L));
        assertEquals(ResultCode.ADDRESS_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void getAddressById_ownershipMismatch_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(2L).build());
        when(userAddressMapper.selectById(10L)).thenReturn(sampleAddress);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAddressService.getAddressById(10L));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
    }

    // ---- createAddress ----

    @Test
    void createAddress_defaultIsDefault0_shouldInsert() {
        UserAddressCreateRequest request = UserAddressCreateRequest.builder()
                .receiverName("李四").receiverPhone("13900139000")
                .province("北京市").city("北京市").district("朝阳区")
                .detailAddress("建国路2号").build();

        when(userAddressMapper.insert(any(UserAddress.class))).thenAnswer(inv -> {
            UserAddress addr = inv.getArgument(0);
            addr.setId(11L);
            return 1;
        });

        Long id = userAddressService.createAddress(request);

        assertEquals(11L, id);
        ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
        verify(userAddressMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getIsDefault());
        assertEquals(1L, captor.getValue().getUserId());
    }

    @Test
    void createAddress_isDefault1_shouldClearOtherDefaults() {
        UserAddressCreateRequest request = UserAddressCreateRequest.builder()
                .receiverName("王五").receiverPhone("13700137000")
                .province("上海市").city("上海市").district("浦东新区")
                .detailAddress("陆家嘴3号").isDefault(1).build();

        when(userAddressMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(userAddressMapper.insert(any(UserAddress.class))).thenAnswer(inv -> {
            UserAddress addr = inv.getArgument(0);
            addr.setId(12L);
            return 1;
        });

        Long id = userAddressService.createAddress(request);

        assertEquals(12L, id);
        verify(userAddressMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    // ---- updateAddress ----

    @Test
    void updateAddress_success_shouldUpdateByAddressId() {
        UserAddressUpdateRequest request = UserAddressUpdateRequest.builder()
                .addressId(10L).receiverName("张三丰").receiverPhone("13800138001")
                .province("广东省").city("广州市").district("天河区")
                .detailAddress("天河路4号").isDefault(0).build();

        when(userAddressMapper.selectById(10L)).thenReturn(sampleAddress);
        when(userAddressMapper.updateById(any(UserAddress.class))).thenReturn(1);

        userAddressService.updateAddress(request);

        ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
        verify(userAddressMapper).updateById(captor.capture());
        assertEquals("张三丰", captor.getValue().getReceiverName());
        assertEquals("广州市", captor.getValue().getCity());
    }

    @Test
    void updateAddress_notFound_shouldThrow() {
        UserAddressUpdateRequest request = UserAddressUpdateRequest.builder()
                .addressId(999L).receiverName("X").receiverPhone("13800138000")
                .province("A").city("B").district("C").detailAddress("D").build();
        when(userAddressMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAddressService.updateAddress(request));
        assertEquals(ResultCode.ADDRESS_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateAddress_ownershipMismatch_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(2L).build());
        UserAddressUpdateRequest request = UserAddressUpdateRequest.builder()
                .addressId(10L).receiverName("X").receiverPhone("13800138000")
                .province("A").city("B").district("C").detailAddress("D").build();
        when(userAddressMapper.selectById(10L)).thenReturn(sampleAddress);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAddressService.updateAddress(request));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    void updateAddress_switchToDefault_shouldClearOthers() {
        UserAddressUpdateRequest request = UserAddressUpdateRequest.builder()
                .addressId(10L).receiverName("张三").receiverPhone("13800138000")
                .province("广东省").city("深圳市").district("南山区")
                .detailAddress("科技园路1号").isDefault(1).build();

        when(userAddressMapper.selectById(10L)).thenReturn(sampleAddress);
        when(userAddressMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(userAddressMapper.updateById(any(UserAddress.class))).thenReturn(1);

        userAddressService.updateAddress(request);

        verify(userAddressMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    // ---- deleteAddress ----

    @Test
    void deleteAddress_success_shouldDelete() {
        when(userAddressMapper.selectById(10L)).thenReturn(sampleAddress);
        when(userAddressMapper.deleteById(10L)).thenReturn(1);

        userAddressService.deleteAddress(10L);

        verify(userAddressMapper).deleteById(10L);
    }

    @Test
    void deleteAddress_notFound_shouldThrow() {
        when(userAddressMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAddressService.deleteAddress(999L));
        assertEquals(ResultCode.ADDRESS_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void deleteAddress_ownershipMismatch_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(2L).build());
        when(userAddressMapper.selectById(10L)).thenReturn(sampleAddress);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAddressService.deleteAddress(10L));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
    }
}
