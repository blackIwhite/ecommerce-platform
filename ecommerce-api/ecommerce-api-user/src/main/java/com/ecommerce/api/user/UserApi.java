package com.ecommerce.api.user;

import com.ecommerce.api.user.dto.UserAddressDTO;
import com.ecommerce.api.user.dto.UserDTO;
import com.ecommerce.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ecommerce-user")
public interface UserApi {

    @GetMapping("/user/{userId}")
    Result<UserDTO> getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/user/address/{addressId}")
    Result<UserAddressDTO> getAddressById(@PathVariable("addressId") Long addressId);
}
