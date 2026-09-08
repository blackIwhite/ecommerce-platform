package com.ecommerce.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_admin_user_role")
public class AdminUserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminUserId;

    private Long roleId;
}
