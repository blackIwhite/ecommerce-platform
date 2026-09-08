package com.ecommerce.common.web.aspect;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.AuditLogMessage;
import com.ecommerce.common.web.context.UserContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Collects audit information around methods annotated with {@link AuditLog} and
 * publishes it to RabbitMQ (audit.exchange / audit.log). The user service consumes
 * the events and persists them into the centralized ecommerce_user.t_audit_log table.
 *
 * <p>If no {@link RabbitTemplate} is available (service without AMQP support), the
 * event is written as a structured log line instead, so auditing never breaks the
 * business call.</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
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
        AuditLogMessage message = AuditLogMessage.builder()
                .serviceName(service)
                .module(module)
                .operation(operation)
                .description(description)
                .method(method)
                .requestParams(truncate(params, 2000))
                .responseCode(code)
                .userId(userId)
                .ip(ip)
                .duration(duration)
                .createTime(System.currentTimeMillis())
                .build();
        try {
            RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
            if (rabbitTemplate != null) {
                rabbitTemplate.convertAndSend(MqConstants.AUDIT_EXCHANGE, MqConstants.AUDIT_ROUTING_KEY, message);
            } else {
                // No RabbitMQ on the classpath/context: fall back to structured logging.
                log.info("AUDIT service={} module={} operation={} description={} method={} responseCode={} userId={} ip={} duration={}ms createTime={} params={}",
                        message.getServiceName(), message.getModule(), message.getOperation(), message.getDescription(),
                        message.getMethod(), message.getResponseCode(), message.getUserId(), message.getIp(),
                        message.getDuration(), message.getCreateTime(), message.getRequestParams());
            }
        } catch (Throwable e) {
            // Audit must never break the business call.
            log.warn("Failed to publish audit log: {}", e.getMessage());
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
