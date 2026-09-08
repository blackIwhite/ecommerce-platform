package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ArticleDTO implements Serializable {

    private Long id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String slug;

    private String content;

    @Size(max = 500)
    private String summary;

    @Size(max = 500)
    private String coverImage;

    private Long categoryId;
    private String categoryName;

    @Size(max = 100)
    private String author;

    /** 0=draft, 1=published, 2=archived */
    private Integer status;
    private Integer sortOrder;
    private Integer viewCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
