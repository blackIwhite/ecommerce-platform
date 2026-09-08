package com.ecommerce.product.entity;

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
@TableName("t_article")
public class Article extends BaseEntity {

    private String title;
    /** URL friendly identifier */
    private String slug;
    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private String author;
    /** 0=draft, 1=published, 2=archived */
    private Integer status;
    private Integer sortOrder;
    private Integer viewCount;
    private LocalDateTime publishTime;
}
