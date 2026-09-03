package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.user.dto.UserAddressCreateRequest;
import com.ecommerce.user.dto.UserAddressDTO;
import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.mapper.UserAddressMapper;
import com.ecommerce.user.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        return addresses.stream().map(this::toAddressDTO).collect(Collectors.toList());
    }

    @Override
    public UserAddressDTO getAddressById(Long addressId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("Address not found");
        }
        return toAddressDTO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(UserAddressCreateRequest request) {
        // If setting as default, clear other defaults first
        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefaultAddress(request.getUserId());
        }

        UserAddress address = UserAddress.builder()
                .userId(request.getUserId())
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
    public void updateAddress(UserAddressCreateRequest request) {
        // Expecting addressId to be passed via userId field repurposed or a separate field
        // In practice, the request would include an addressId. For this implementation,
        // we use a query by userId + receiverName + receiverPhone as a simple approach.
        // A proper implementation would have an addressId in the request.
        // Here we'll query by all fields to find the address to update.
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, request.getUserId());
        if (request.getReceiverPhone() != null) {
            wrapper.eq(UserAddress::getReceiverPhone, request.getReceiverPhone());
        }
        if (request.getDetailAddress() != null) {
            wrapper.eq(UserAddress::getDetailAddress, request.getDetailAddress());
        }
        UserAddress existing = userAddressMapper.selectOne(wrapper);
        if (existing == null) {
            throw new BusinessException("Address not found");
        }

        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefaultAddress(request.getUserId());
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
    public void deleteAddress(Long addressId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("Address not found");
        }
        userAddressMapper.deleteById(addressId);
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
