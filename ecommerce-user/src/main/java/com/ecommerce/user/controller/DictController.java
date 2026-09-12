package com.ecommerce.user.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.user.dto.DictItemDTO;
import com.ecommerce.user.dto.DictTypeDTO;
import com.ecommerce.user.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/dict")
@RequiredArgsConstructor
@Tag(name = "数据字典", description = "Data dictionary APIs")
public class DictController {

    private final DictService dictService;

    @Operation(summary = "字典类型分页")
    @RequireLogin
    @GetMapping("/type/page")
    public Result<PageResult<DictTypeDTO>> pageTypes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(dictService.pageTypes(pageNum, pageSize));
    }

    @Operation(summary = "字典类型列表(全部)")
    @GetMapping("/type/list")
    public Result<List<DictTypeDTO>> listTypes() {
        return Result.success(dictService.listAllTypes());
    }

    @AuditLog(module = "字典", operation = "创建字典类型", description = "创建字典类型")
    @RequireLogin
    @PostMapping("/type")
    public Result<Long> createType(@RequestBody DictTypeDTO dto) {
        return Result.success(dictService.createType(dto));
    }

    @AuditLog(module = "字典", operation = "更新字典类型", description = "更新字典类型")
    @RequireLogin
    @PutMapping("/type")
    public Result<Void> updateType(@RequestBody DictTypeDTO dto) {
        dictService.updateType(dto);
        return Result.success();
    }

    @AuditLog(module = "字典", operation = "删除字典类型", description = "删除字典类型及其字典项")
    @RequireLogin
    @DeleteMapping("/type/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return Result.success();
    }

    @Operation(summary = "字典项列表")
    @GetMapping("/item/list")
    public Result<List<DictItemDTO>> listItems(@RequestParam String typeCode) {
        return Result.success(dictService.listItems(typeCode));
    }

    @AuditLog(module = "字典", operation = "创建字典项", description = "创建字典项")
    @RequireLogin
    @PostMapping("/item")
    public Result<Long> createItem(@RequestBody DictItemDTO dto) {
        return Result.success(dictService.createItem(dto));
    }

    @AuditLog(module = "字典", operation = "更新字典项", description = "更新字典项")
    @RequireLogin
    @PutMapping("/item")
    public Result<Void> updateItem(@RequestBody DictItemDTO dto) {
        dictService.updateItem(dto);
        return Result.success();
    }

    @AuditLog(module = "字典", operation = "删除字典项", description = "删除字典项")
    @RequireLogin
    @DeleteMapping("/item/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return Result.success();
    }

    @AuditLog(module = "字典", operation = "修改字典项状态", description = "修改字典项状态")
    @RequireLogin
    @PutMapping("/item/{id}/status")
    public Result<Void> updateItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        dictService.updateItemStatus(id, status);
        return Result.success();
    }
}
