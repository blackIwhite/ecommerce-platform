package com.ecommerce.file.controller;

import com.ecommerce.common.core.result.Result;
import com.ecommerce.file.dto.FileDTO;
import com.ecommerce.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file/internal")
@RequiredArgsConstructor
public class FileInternalController {

    private final FileService fileService;

    @GetMapping("/url/{id}")
    public Result<FileDTO> getFileUrl(@PathVariable Long id) {
        return Result.success(fileService.getById(id));
    }
}
