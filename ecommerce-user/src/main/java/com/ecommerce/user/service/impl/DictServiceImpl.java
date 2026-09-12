package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.user.dto.DictItemDTO;
import com.ecommerce.user.dto.DictTypeDTO;
import com.ecommerce.user.entity.DictItem;
import com.ecommerce.user.entity.DictType;
import com.ecommerce.user.mapper.DictItemMapper;
import com.ecommerce.user.mapper.DictTypeMapper;
import com.ecommerce.user.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictTypeMapper typeMapper;
    private final DictItemMapper itemMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<DictTypeDTO> pageTypes(int pageNum, int pageSize) {
        Page<DictType> page = typeMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<DictType>().orderByDesc(DictType::getCreateTime));
        List<DictTypeDTO> list = page.getRecords().stream().map(this::toTypeDTO).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<DictTypeDTO> listAllTypes() {
        return typeMapper.selectList(
                new LambdaQueryWrapper<DictType>().eq(DictType::getStatus, 1).orderByAsc(DictType::getTypeCode))
                .stream().map(this::toTypeDTO).toList();
    }

    @Override
    public Long createType(DictTypeDTO dto) {
        long count = typeMapper.selectCount(
                new LambdaQueryWrapper<DictType>().eq(DictType::getTypeCode, dto.getTypeCode()));
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "字典类型编码已存在");
        }
        DictType type = DictType.builder()
                .typeCode(dto.getTypeCode()).typeName(dto.getTypeName())
                .remark(dto.getRemark()).status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();
        typeMapper.insert(type);
        return type.getId();
    }

    @Override
    public void updateType(DictTypeDTO dto) {
        DictType type = typeMapper.selectById(dto.getId());
        if (type == null) throw new BusinessException(ResultCode.PARAM_ERROR, "字典类型不存在");
        type.setTypeName(dto.getTypeName());
        type.setRemark(dto.getRemark());
        if (dto.getStatus() != null) type.setStatus(dto.getStatus());
        typeMapper.updateById(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        DictType type = typeMapper.selectById(id);
        if (type == null) throw new BusinessException(ResultCode.PARAM_ERROR, "字典类型不存在");
        typeMapper.deleteById(id);
        itemMapper.delete(new LambdaQueryWrapper<DictItem>().eq(DictItem::getTypeCode, type.getTypeCode()));
    }

    @Override
    public List<DictItemDTO> listItems(String typeCode) {
        return itemMapper.selectList(
                new LambdaQueryWrapper<DictItem>()
                        .eq(DictItem::getTypeCode, typeCode)
                        .orderByAsc(DictItem::getSortOrder))
                .stream().map(this::toItemDTO).toList();
    }

    @Override
    public Long createItem(DictItemDTO dto) {
        DictItem item = DictItem.builder()
                .typeCode(dto.getTypeCode()).itemValue(dto.getItemValue())
                .itemLabel(dto.getItemLabel()).sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : 1).remark(dto.getRemark())
                .build();
        itemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateItem(DictItemDTO dto) {
        DictItem item = itemMapper.selectById(dto.getId());
        if (item == null) throw new BusinessException(ResultCode.PARAM_ERROR, "字典项不存在");
        item.setItemLabel(dto.getItemLabel());
        item.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) item.setStatus(dto.getStatus());
        item.setRemark(dto.getRemark());
        itemMapper.updateById(item);
    }

    @Override
    public void deleteItem(Long id) {
        itemMapper.deleteById(id);
    }

    @Override
    public void updateItemStatus(Long id, Integer status) {
        DictItem item = itemMapper.selectById(id);
        if (item == null) throw new BusinessException(ResultCode.PARAM_ERROR, "字典项不存在");
        item.setStatus(status);
        itemMapper.updateById(item);
    }

    private DictTypeDTO toTypeDTO(DictType t) {
        return DictTypeDTO.builder()
                .id(t.getId()).typeCode(t.getTypeCode()).typeName(t.getTypeName())
                .remark(t.getRemark()).status(t.getStatus())
                .createTime(t.getCreateTime().format(FMT))
                .build();
    }

    private DictItemDTO toItemDTO(DictItem i) {
        return DictItemDTO.builder()
                .id(i.getId()).typeCode(i.getTypeCode()).itemValue(i.getItemValue())
                .itemLabel(i.getItemLabel()).sortOrder(i.getSortOrder())
                .status(i.getStatus()).remark(i.getRemark())
                .build();
    }
}
