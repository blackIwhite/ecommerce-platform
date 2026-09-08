package com.ecommerce.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Search history record. Does not extend BaseEntity because the table
 * has no update_time / deleted columns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_search_history")
public class SearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String keyword;
    private LocalDateTime searchTime;
}
