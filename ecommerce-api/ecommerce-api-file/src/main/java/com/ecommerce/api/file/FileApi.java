package com.ecommerce.api.file;

import com.ecommerce.api.file.dto.FileDTO;
import com.ecommerce.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ecommerce-file")
public interface FileApi {

    @GetMapping("/file/internal/url/{id}")
    Result<FileDTO> getFileUrl(@PathVariable("id") Long id);
}
