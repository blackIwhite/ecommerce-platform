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
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRecordMapper fileRecordMapper;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Override
    public FileDTO upload(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String storagePath = UUID.randomUUID().toString().replace("-", "") + extension;
            String contentType = file.getContentType();

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(storagePath)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            String url = endpoint + "/" + bucket + "/" + storagePath;

            FileRecord record = FileRecord.builder()
                    .originalName(originalName)
                    .storagePath(storagePath)
                    .fileSize(file.getSize())
                    .contentType(contentType)
                    .bucket(bucket)
                    .url(url)
                    .build();
            fileRecordMapper.insert(record);

            log.info("File uploaded: {} -> {}", originalName, storagePath);
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
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(record.getBucket())
                    .object(record.getStoragePath())
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", record.getStoragePath(), e);
            throw new BusinessException(ResultCode.FILE_DELETE_FAILED);
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
