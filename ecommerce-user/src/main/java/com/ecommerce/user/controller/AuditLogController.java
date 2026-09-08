package com.ecommerce.user.controller;

import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/admin/audit-log")
@RequiredArgsConstructor
@RequireLogin
public class AuditLogController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listAuditLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        StringBuilder sql = new StringBuilder("SELECT * FROM t_audit_log WHERE 1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (serviceName != null && !serviceName.isEmpty()) {
            sql.append(" AND service_name = ?");
            params.add(serviceName);
        }
        if (module != null && !module.isEmpty()) {
            sql.append(" AND module = ?");
            params.add(module);
        }
        if (userId != null && !userId.isEmpty()) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        if (startTime != null && !startTime.isEmpty()) {
            sql.append(" AND create_time >= ?");
            params.add(startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            sql.append(" AND create_time <= ?");
            params.add(endTime);
        }

        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNum - 1) * pageSize);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return Result.success(rows);
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> countAuditLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String module) {

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as total FROM t_audit_log WHERE 1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (serviceName != null && !serviceName.isEmpty()) {
            sql.append(" AND service_name = ?");
            params.add(serviceName);
        }
        if (module != null && !module.isEmpty()) {
            sql.append(" AND module = ?");
            params.add(module);
        }

        Map<String, Object> count = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
        return Result.success(count);
    }
}
