package com.ecommerce.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.file.dto.FileDTO;
import com.ecommerce.file.entity.FileRecord;
import com.ecommerce.file.mapper.FileRecordMapper;
import com.ecommerce.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRecordMapper fileRecordMapper;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/api/file}")
    private String baseUrl;

    @Override
    public FileDTO upload(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String storageName = UUID.randomUUID().toString().replace("-", "") + extension;
            String contentType = file.getContentType();

            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(storageName).toFile());

            String url = baseUrl + "/file/download/" + storageName;

            FileRecord record = FileRecord.builder()
                    .originalName(originalName)
                    .storagePath(storageName)
                    .fileSize(file.getSize())
                    .contentType(contentType)
                    .bucket("local")
                    .url(url)
                    .build();
            fileRecordMapper.insert(record);

            log.info("File uploaded: {} -> {}", originalName, storageName);
            return toDTO(record);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(Long id) {
        FileRecord record = fileRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        try {
            Path filePath = Paths.get(uploadDir, record.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete local file: {}", record.getStoragePath(), e);
        }
        fileRecordMapper.deleteById(id);
        log.info("File deleted: id={}, path={}", id, record.getStoragePath());
    }

    @Override
    public FileDTO getById(Long id) {
        FileRecord record = fileRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return toDTO(record);
    }

    @Override
    public PageResult<FileDTO> list(int pageNum, int pageSize) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FileRecord::getCreateTime);
        Page<FileRecord> page = fileRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        var records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    public Path resolveFile(String storageName) {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path resolved = base.resolve(storageName).normalize();
        if (!resolved.startsWith(base)) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return resolved;
    }

    private FileDTO toDTO(FileRecord record) {
        return FileDTO.builder()
                .id(record.getId())
                .originalName(record.getOriginalName())
                .url(record.getUrl())
                .fileSize(record.getFileSize())
                .contentType(record.getContentType())
                .createTime(record.getCreateTime())
                .build();
    }
}
