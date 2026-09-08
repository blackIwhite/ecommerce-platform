package com.ecommerce.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_message")
public class UserMessage extends BaseEntity {

    private Long userId;
    private Integer type;
    private String title;
    private String content;
    private Long referenceId;
    private Integer isRead;
    private LocalDateTime readTime;
}
