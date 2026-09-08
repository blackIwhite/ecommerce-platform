package com.ecommerce.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_file")
public class FileRecord extends BaseEntity {
    private String originalName;
    private String storagePath;
    private Long fileSize;
    private String contentType;
    private String bucket;
    private String url;
}
