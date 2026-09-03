package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserDTO;
import com.ecommerce.user.dto.UserUpdateRequest;

public interface UserService {

    UserDTO getUserById(Long userId);

    void updateUser(UserUpdateRequest request);
}
