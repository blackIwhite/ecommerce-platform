package com.ecommerce.common.web.aspect;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.web.context.UserContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String userId = safeGetUserId();
        String ip = safeGetIp();
        String method = joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName();
        String params = safeGetParams(joinPoint);
        int code = 200;
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            code = 500;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            saveLog(serviceName, auditLog.module(), auditLog.operation(), auditLog.description(),
                    method, params, code, userId, ip, duration);
        }
    }

    private void saveLog(String service, String module, String operation, String description,
                         String method, String params, int code, String userId, String ip, long duration) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO t_audit_log (service_name, module, operation, description, method, request_params, response_code, user_id, ip, duration, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    service, module, operation, description, method,
                    truncate(params, 2000), code, userId, ip, duration, LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("Failed to save audit log: {}", e.getMessage());
        }
    }

    private String safeGetUserId() {
        try {
            Long id = UserContextHolder.getUserId();
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String safeGetIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return null;
        }
    }

    private String safeGetParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature sig = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = sig.getParameterNames();
            Object[] args = joinPoint.getArgs();
            if (paramNames == null || paramNames.length == 0) return "";
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < paramNames.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append("\"").append(paramNames[i]).append("\":");
                try {
                    sb.append(objectMapper.writeValueAsString(args[i]));
                } catch (Exception e) {
                    sb.append("\"").append(args[i]).append("\"");
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
