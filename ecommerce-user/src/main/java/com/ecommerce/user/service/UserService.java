package com.ecommerce.user.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.user.dto.UserDTO;
import com.ecommerce.user.dto.UserPageRequest;
import com.ecommerce.user.dto.UserUpdateRequest;

public interface UserService {

    UserDTO getUserById(Long userId);

    void updateUser(UserUpdateRequest request);

    PageResult<UserDTO> pageUsers(UserPageRequest request);

    void updateUserStatus(Long userId, Integer status);
}
