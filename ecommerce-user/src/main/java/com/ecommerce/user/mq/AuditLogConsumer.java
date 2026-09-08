package com.ecommerce.user.mq;

import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.AuditLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Centralized audit log consumer: receives audit events published by every service
 * (via AuditLogAspect -> audit.exchange) and persists them into ecommerce_user.t_audit_log,
 * which the admin AuditLogController reads.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = MqConstants.AUDIT_QUEUE)
    public void onAuditLog(AuditLogMessage message) {
        try {
            LocalDateTime createTime = message.getCreateTime() != null
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getCreateTime()), ZoneId.systemDefault())
                    : LocalDateTime.now();
            jdbcTemplate.update(
                    "INSERT INTO t_audit_log (service_name, module, operation, description, method, request_params, response_code, user_id, ip, duration, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    message.getServiceName(), message.getModule(), message.getOperation(), message.getDescription(),
                    message.getMethod(), message.getRequestParams(), message.getResponseCode(),
                    message.getUserId(), message.getIp(), message.getDuration(), createTime
            );
        } catch (Exception e) {
            // Log and swallow: rethrowing would requeue the message and could loop
            // forever on poison messages (e.g. missing table, bad payload).
            log.error("Failed to persist audit log [service={}, module={}, operation={}]: {}",
                    message.getServiceName(), message.getModule(), message.getOperation(), e.getMessage(), e);
        }
    }
}
