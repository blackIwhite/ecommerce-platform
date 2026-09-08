package com.ecommerce.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.product.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
