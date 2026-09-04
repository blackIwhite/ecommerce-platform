package com.ecommerce.order.config;

import com.ecommerce.common.core.constant.CommonConstants;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignRelayConfig {

    @Bean
    public RequestInterceptor userContextRelayInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                copyHeader(request, template, CommonConstants.USER_ID_HEADER);
                copyHeader(request, template, CommonConstants.PHONE_HEADER);
            }
        };
    }

    private void copyHeader(HttpServletRequest request, feign.RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isEmpty()) {
            template.header(headerName, value);
        }
    }
}
