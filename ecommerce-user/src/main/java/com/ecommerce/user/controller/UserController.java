package com.ecommerce.user.controller;

import com.ecommerce.common.core.result.Result;
import com.ecommerce.user.dto.UserAddressCreateRequest;
import com.ecommerce.user.dto.UserAddressDTO;
import com.ecommerce.user.dto.UserDTO;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.service.UserAddressService;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAddressService userAddressService;

    @GetMapping("/{userId}")
    public Result<UserDTO> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @PutMapping
    public Result<Void> updateUser(@RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        return Result.success();
    }

    @GetMapping("/address/list")
    public Result<List<UserAddressDTO>> getAddressList(@RequestParam Long userId) {
        return Result.success(userAddressService.getAddressList(userId));
    }

    @GetMapping("/address/{addressId}")
    public Result<UserAddressDTO> getAddressById(@PathVariable Long addressId) {
        return Result.success(userAddressService.getAddressById(addressId));
    }

    @PostMapping("/address")
    public Result<Long> createAddress(@RequestBody UserAddressCreateRequest request) {
        return Result.success(userAddressService.createAddress(request));
    }

    @PutMapping("/address")
    public Result<Void> updateAddress(@RequestBody UserAddressCreateRequest request) {
        userAddressService.updateAddress(request);
        return Result.success();
    }

    @DeleteMapping("/address/{addressId}")
    public Result<Void> deleteAddress(@PathVariable Long addressId) {
        userAddressService.deleteAddress(addressId);
        return Result.success();
    }
}
