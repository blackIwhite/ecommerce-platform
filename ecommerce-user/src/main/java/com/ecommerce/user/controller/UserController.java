package com.ecommerce.user.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.annotation.ShowSensitive;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.user.dto.UserAddressCreateRequest;
import com.ecommerce.user.dto.UserAddressDTO;
import com.ecommerce.user.dto.UserAddressUpdateRequest;
import com.ecommerce.user.dto.UserDTO;
import com.ecommerce.user.dto.UserFavoriteDTO;
import com.ecommerce.user.dto.UserPageRequest;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.service.UserAddressService;
import com.ecommerce.user.service.UserFavoriteService;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "User management APIs")
public class UserController {

    private final UserService userService;
    private final UserAddressService userAddressService;
    private final UserFavoriteService userFavoriteService;

    @GetMapping("/{userId}")
    public Result<UserDTO> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @RequireLogin
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser() {
        return Result.success(userService.getUserById(UserContextHolder.getUserId()));
    }

    @AuditLog(module = "用户", operation = "更新用户", description = "更新用户信息")
    @RequireLogin
    @PutMapping
    public Result<Void> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        userService.updateUser(request);
        return Result.success();
    }

    @RequireLogin
    @ShowSensitive
    @GetMapping("/page")
    public Result<PageResult<UserDTO>> pageUsers(UserPageRequest request) {
        return Result.success(userService.pageUsers(request));
    }

    @AuditLog(module = "用户", operation = "更新用户状态", description = "更新用户状态")
    @RequireLogin
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success();
    }

    @RequireLogin
    @GetMapping("/address/list")
    public Result<List<UserAddressDTO>> getAddressList() {
        return Result.success(userAddressService.getAddressList(UserContextHolder.getUserId()));
    }

    @GetMapping("/address/{addressId}")
    public Result<UserAddressDTO> getAddressById(@PathVariable Long addressId) {
        return Result.success(userAddressService.getAddressById(addressId));
    }

    @RequireLogin
    @PostMapping("/address")
    public Result<Long> createAddress(@RequestBody @Valid UserAddressCreateRequest request) {
        return Result.success(userAddressService.createAddress(request));
    }

    @RequireLogin
    @PutMapping("/address")
    public Result<Void> updateAddress(@RequestBody @Valid UserAddressUpdateRequest request) {
        userAddressService.updateAddress(request);
        return Result.success();
    }

    @RequireLogin
    @DeleteMapping("/address/{addressId}")
    public Result<Void> deleteAddress(@PathVariable Long addressId) {
        userAddressService.deleteAddress(addressId);
        return Result.success();
    }

    @RequireLogin
    @PostMapping("/favorite/toggle")
    public Result<Boolean> toggleFavorite(@RequestParam Long spuId) {
        return Result.success(userFavoriteService.toggleFavorite(spuId));
    }

    @RequireLogin
    @GetMapping("/favorite/check")
    public Result<Boolean> checkFavorite(@RequestParam Long spuId) {
        return Result.success(userFavoriteService.isFavorite(spuId));
    }

    @RequireLogin
    @GetMapping("/favorite/list")
    public Result<PageResult<UserFavoriteDTO>> listFavorites(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(userFavoriteService.listFavorites(pageNum, pageSize));
    }
}
