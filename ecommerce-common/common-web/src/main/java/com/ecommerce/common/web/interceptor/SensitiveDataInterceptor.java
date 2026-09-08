package com.ecommerce.common.web.interceptor;

import com.ecommerce.common.core.serializer.SensitiveDataSerializer;
import com.ecommerce.common.web.annotation.ShowSensitive;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SensitiveDataInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            ShowSensitive methodAnnotation = handlerMethod.getMethodAnnotation(ShowSensitive.class);
            ShowSensitive classAnnotation = handlerMethod.getBeanType().getAnnotation(ShowSensitive.class);
            if (methodAnnotation != null || classAnnotation != null) {
                SensitiveDataSerializer.setShowSensitive(true);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SensitiveDataSerializer.setShowSensitive(false);
    }
}
