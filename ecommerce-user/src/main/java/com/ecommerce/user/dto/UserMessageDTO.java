package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageDTO implements Serializable {

    private Long id;
    private Long userId;
    private Integer type;
    private String typeName;
    private String title;
    private String content;
    private Long referenceId;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
