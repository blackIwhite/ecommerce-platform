package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;

    private String roleName;

    private String roleKey;

    private String description;

    private Integer status;

    private Integer sort;

    private List<Long> permissionIds;

    private List<Long> menuIds;
}
