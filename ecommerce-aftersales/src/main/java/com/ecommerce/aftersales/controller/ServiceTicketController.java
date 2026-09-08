package com.ecommerce.aftersales.controller;

import com.ecommerce.aftersales.service.ServiceTicketService;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class ServiceTicketController {

    private final ServiceTicketService ticketService;

    @PostMapping
    @RequireLogin
    public Result<Long> create(@RequestBody Map<String, Object> request) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(ticketService.create(userId, request));
    }

    @GetMapping("/list")
    @RequireLogin
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(ticketService.listByUser(userId, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @RequireLogin
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(ticketService.getDetail(id, userId));
    }

    @PostMapping("/{id}/message")
    @RequireLogin
    public Result<Long> sendMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(ticketService.sendMessage(id, userId, body.get("content")));
    }

    @GetMapping("/{id}/messages")
    @RequireLogin
    public Result<List<Map<String, Object>>> messages(@PathVariable Long id) {
        return Result.success(ticketService.getMessages(id));
    }
}
