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
public class MenuDTO {

    private Long id;

    private Long parentId;

    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer sort;

    /**
     * Menu type: 1=directory, 2=menu, 3=button.
     */
    private Integer type;

    private String permission;

    private Integer visible;

    private Integer status;

    private List<MenuDTO> children;
}
