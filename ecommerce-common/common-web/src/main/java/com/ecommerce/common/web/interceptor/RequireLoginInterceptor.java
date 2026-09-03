package com.ecommerce.common.web.interceptor;

import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequireLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireLogin methodAnnotation = handlerMethod.getMethodAnnotation(RequireLogin.class);
        RequireLogin classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);

        if (methodAnnotation != null || classAnnotation != null) {
            if (UserContextHolder.getUserId() == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }
        }
        return true;
    }
}
