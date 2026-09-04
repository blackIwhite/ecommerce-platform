package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest implements Serializable {

    @NotNull(message = "User id is required")
    private Long userId;

    @Size(max = 50)
    private String nickname;

    @Size(max = 500)
    private String avatar;
}
