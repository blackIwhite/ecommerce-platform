package com.ecommerce.api.user.dto;

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
public class UserAddressDTO implements Serializable {

    private Long addressId;
    private Long userId;
    @SensitiveData(type = SensitiveData.SensitiveType.NAME)
    private String receiverName;
    @SensitiveData(type = SensitiveData.SensitiveType.PHONE)
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    @SensitiveData(type = SensitiveData.SensitiveType.ADDRESS)
    private String detailAddress;
    private Integer isDefault;
}
