package com.ecommerce.common.web.config;

import com.ecommerce.common.web.interceptor.SlowQueryInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {

    private final SqlSessionFactory sqlSessionFactory;
    private final SlowQueryInterceptor slowQueryInterceptor;

    @PostConstruct
    public void addInterceptors() {
        sqlSessionFactory.getConfiguration().addInterceptor(slowQueryInterceptor);
    }
}
