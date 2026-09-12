package com.ecommerce.file.controller;

import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.file.service.impl.FileServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/file/download")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileServiceImpl fileService;

    @GetMapping("/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String fileName) {
        try {
            Path filePath = fileService.resolveFile(fileName);
            if (!Files.exists(filePath)) {
                throw new BusinessException(ResultCode.FILE_NOT_FOUND);
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            UrlResource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to serve file: {}", fileName, e);
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
    }
}
