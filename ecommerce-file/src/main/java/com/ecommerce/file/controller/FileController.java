package com.ecommerce.file.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.file.dto.FileDTO;
import com.ecommerce.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @AuditLog(module = "文件", operation = "上传文件", description = "上传文件")
    @PostMapping("/upload")
    public Result<FileDTO> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.upload(file));
    }

    @AuditLog(module = "文件", operation = "删除文件", description = "删除文件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<FileDTO> getById(@PathVariable Long id) {
        return Result.success(fileService.getById(id));
    }

    @GetMapping("/list")
    public Result<PageResult<FileDTO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(fileService.list(pageNum, pageSize));
    }
}
