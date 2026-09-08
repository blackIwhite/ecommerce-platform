package com.ecommerce.api.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO implements Serializable {
    private Long id;
    private String originalName;
    private String url;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createTime;
}
