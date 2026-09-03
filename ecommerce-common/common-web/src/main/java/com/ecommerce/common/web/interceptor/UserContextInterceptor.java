package com.ecommerce.common.web.interceptor;

import com.ecommerce.common.core.constant.CommonConstants;
import com.ecommerce.common.web.context.UserContext;
import com.ecommerce.common.web.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader(CommonConstants.USER_ID_HEADER);
        String phone = request.getHeader(CommonConstants.PHONE_HEADER);

        if (userIdStr != null && !userIdStr.isEmpty()) {
            UserContext context = UserContext.builder()
                    .userId(Long.parseLong(userIdStr))
                    .phone(phone)
                    .build();
            UserContextHolder.set(context);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
