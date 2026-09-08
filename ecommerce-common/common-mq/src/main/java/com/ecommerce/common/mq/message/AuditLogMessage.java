package com.ecommerce.common.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Audit log event message body published by AuditLogAspect in every service
 * and consumed by the user service, which persists it into ecommerce_user.t_audit_log.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Source service name (spring.application.name). */
    private String serviceName;

    /** Business module from the @AuditLog annotation. */
    private String module;

    /** Operation name from the @AuditLog annotation. */
    private String operation;

    /** Operation description from the @AuditLog annotation. */
    private String description;

    /** Target class and method being invoked. */
    private String method;

    /** Serialized request parameters (truncated to 2000 chars). */
    private String requestParams;

    /** Response code: 200=success, 500=business exception. */
    private Integer responseCode;

    /** Operator user ID, nullable for anonymous calls. */
    private String userId;

    /** Client IP address. */
    private String ip;

    /** Execution duration in milliseconds. */
    private Long duration;

    /** Event timestamp as epoch milliseconds. */
    private Long createTime;
}
