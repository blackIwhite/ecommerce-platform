package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserAddressCreateRequest;
import com.ecommerce.user.dto.UserAddressDTO;

import java.util.List;

public interface UserAddressService {

    List<UserAddressDTO> getAddressList(Long userId);

    UserAddressDTO getAddressById(Long addressId);

    Long createAddress(UserAddressCreateRequest request);

    void updateAddress(UserAddressCreateRequest request);

    void deleteAddress(Long addressId);
}
