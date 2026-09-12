package com.ecommerce.product.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.entity.SensitiveWord;
import com.ecommerce.product.mapper.SensitiveWordMapper;
import com.ecommerce.product.service.SensitiveWordFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/sensitive-word")
@RequiredArgsConstructor
@Tag(name = "敏感词管理", description = "Sensitive word management APIs")
public class SensitiveWordController {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Operation(summary = "敏感词分页")
    @RequireLogin
    @GetMapping("/list")
    public Result<List<SensitiveWord>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize) {
        Page<SensitiveWord> page = sensitiveWordMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SensitiveWord>().orderByDesc(SensitiveWord::getCreateTime));
        return Result.success(page.getRecords());
    }

    @AuditLog(module = "敏感词", operation = "添加敏感词", description = "添加敏感词")
    @RequireLogin
    @PostMapping
    public Result<Void> add(@RequestBody Map<String, String> body) {
        String word = body.get("word");
        if (word == null || word.isBlank()) return Result.success();
        long count = sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getWord, word));
        if (count == 0) {
            sensitiveWordMapper.insert(SensitiveWord.builder().word(word.trim()).build());
            sensitiveWordFilter.reload();
        }
        return Result.success();
    }

    @AuditLog(module = "敏感词", operation = "删除敏感词", description = "删除敏感词")
    @RequireLogin
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordMapper.deleteById(id);
        sensitiveWordFilter.reload();
        return Result.success();
    }

    @AuditLog(module = "敏感词", operation = "批量添加敏感词", description = "批量添加敏感词")
    @RequireLogin
    @PostMapping("/batch")
    public Result<Integer> batchAdd(@RequestBody Map<String, String> body) {
        String words = body.get("words");
        if (words == null || words.isBlank()) return Result.success(0);
        int count = 0;
        for (String w : words.split("[,，\\n]")) {
            String trimmed = w.trim();
            if (trimmed.isEmpty()) continue;
            long existing = sensitiveWordMapper.selectCount(
                    new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getWord, trimmed));
            if (existing == 0) {
                sensitiveWordMapper.insert(SensitiveWord.builder().word(trimmed).build());
                count++;
            }
        }
        if (count > 0) sensitiveWordFilter.reload();
        return Result.success(count);
    }

    @AuditLog(module = "敏感词", operation = "重新加载过滤词库", description = "重新加载敏感词过滤词库")
    @RequireLogin
    @PostMapping("/reload")
    public Result<Void> reload() {
        sensitiveWordFilter.reload();
        return Result.success();
    }

    @Operation(summary = "测试敏感词过滤")
    @RequireLogin
    @PostMapping("/test")
    public Result<Map<String, Object>> testFilter(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        Map<String, Object> result = Map.of(
                "original", text,
                "filtered", sensitiveWordFilter.filter(text),
                "containsSensitive", sensitiveWordFilter.containsSensitiveWord(text)
        );
        return Result.success(result);
    }
}
