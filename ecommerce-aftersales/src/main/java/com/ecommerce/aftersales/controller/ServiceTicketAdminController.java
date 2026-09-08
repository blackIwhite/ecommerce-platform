package com.ecommerce.aftersales.controller;

import com.ecommerce.aftersales.service.ServiceTicketService;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ticket/admin")
@Tag(name = "服务工单(后台)", description = "Service ticket admin APIs")
@RequiredArgsConstructor
public class ServiceTicketAdminController {

    private final ServiceTicketService ticketService;

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(ticketService.listForAdmin(type, status, assignedTo, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(ticketService.getAdminDetail(id));
    }

    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ticketService.assign(id, body.getOrDefault("agent", "admin"));
        return Result.success();
    }

    @PostMapping("/{id}/reply")
    public Result<Long> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String agent = body.getOrDefault("agent", "admin");
        return Result.success(ticketService.adminReply(id, agent, body.get("content")));
    }

    @PutMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String agent = body != null ? body.getOrDefault("agent", "admin") : "admin";
        ticketService.resolve(id, agent);
        return Result.success();
    }

    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String agent = body != null ? body.getOrDefault("agent", "admin") : "admin";
        ticketService.close(id, agent);
        return Result.success();
    }
}
