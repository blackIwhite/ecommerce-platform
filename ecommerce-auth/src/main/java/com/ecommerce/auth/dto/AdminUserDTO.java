package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Integer status;

    private List<String> roles;

    private LocalDateTime createTime;
}
