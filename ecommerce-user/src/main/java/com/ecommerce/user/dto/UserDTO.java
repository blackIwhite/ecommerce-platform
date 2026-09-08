package com.ecommerce.user.dto;

import com.ecommerce.common.core.annotation.SensitiveData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    private Long userId;
    @SensitiveData(type = SensitiveData.SensitiveType.PHONE)
    private String phone;
    private String nickname;
    private String avatar;
    private Integer status;
}
