package com.ecommerce.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_favorite")
public class UserFavorite extends BaseEntity {

    private Long userId;
    private Long spuId;
}
