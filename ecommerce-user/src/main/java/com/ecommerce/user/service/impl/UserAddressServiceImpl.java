package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.user.dto.UserAddressCreateRequest;
import com.ecommerce.user.dto.UserAddressDTO;
import com.ecommerce.user.dto.UserAddressUpdateRequest;
import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.mapper.UserAddressMapper;
import com.ecommerce.user.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddressDTO> getAddressList(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        List<UserAddress> addresses = userAddressMapper.selectList(wrapper);
        return addresses.stream().map(this::toAddressDTO).toList();
    }

    @Override
    public UserAddressDTO getAddressById(Long addressId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }
        checkOwnership(address);
        return toAddressDTO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(UserAddressCreateRequest request) {
        Long ownerUserId = UserContextHolder.getUserId() != null
                ? UserContextHolder.getUserId() : request.getUserId();

        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefaultAddress(ownerUserId);
        }

        UserAddress address = UserAddress.builder()
                .userId(ownerUserId)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detailAddress(request.getDetailAddress())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : 0)
                .build();
        userAddressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(UserAddressUpdateRequest request) {
        UserAddress existing = userAddressMapper.selectById(request.getAddressId());
        if (existing == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }
        checkOwnership(existing);

        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefaultAddress(existing.getUserId());
        }

        existing.setReceiverName(request.getReceiverName());
        existing.setReceiverPhone(request.getReceiverPhone());
        existing.setProvince(request.getProvince());
        existing.setCity(request.getCity());
        existing.setDistrict(request.getDistrict());
        existing.setDetailAddress(request.getDetailAddress());
        if (request.getIsDefault() != null) {
            existing.setIsDefault(request.getIsDefault());
        }
        userAddressMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }
        checkOwnership(address);
        userAddressMapper.deleteById(addressId);
    }

    private void checkOwnership(UserAddress address) {
        Long currentUserId = UserContextHolder.getUserId();
        if (currentUserId != null && !currentUserId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Address does not belong to current user");
        }
    }

    private void clearDefaultAddress(Long userId) {
        LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0);
        userAddressMapper.update(null, updateWrapper);
    }

    private UserAddressDTO toAddressDTO(UserAddress address) {
        return UserAddressDTO.builder()
                .addressId(address.getId())
                .userId(address.getUserId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .isDefault(address.getIsDefault())
                .build();
    }
}
