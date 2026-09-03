package com.ecommerce.common.web.config;

import com.ecommerce.common.web.interceptor.RequestLogInterceptor;
import com.ecommerce.common.web.interceptor.RequireLoginInterceptor;
import com.ecommerce.common.web.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestLogInterceptor requestLogInterceptor;
    private final UserContextInterceptor userContextInterceptor;
    private final RequireLoginInterceptor requireLoginInterceptor;

    public WebMvcConfig(RequestLogInterceptor requestLogInterceptor,
                        UserContextInterceptor userContextInterceptor,
                        RequireLoginInterceptor requireLoginInterceptor) {
        this.requestLogInterceptor = requestLogInterceptor;
        this.userContextInterceptor = userContextInterceptor;
        this.requireLoginInterceptor = requireLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .order(Ordered.HIGHEST_PRECEDENCE);
        registry.addInterceptor(requireLoginInterceptor)
                .addPathPatterns("/**")
                .order(Ordered.HIGHEST_PRECEDENCE + 1);
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**")
                .order(Ordered.HIGHEST_PRECEDENCE + 2);
    }
}
