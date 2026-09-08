package com.ecommerce.file.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.file.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileDTO upload(MultipartFile file);

    void delete(Long id);

    FileDTO getById(Long id);

    PageResult<FileDTO> list(int pageNum, int pageSize);
}
