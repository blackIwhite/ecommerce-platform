package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.product.dto.ShippingFeeCalculateResult;
import com.ecommerce.product.dto.ShippingTemplateCreateRequest;
import com.ecommerce.product.dto.ShippingTemplateDTO;
import com.ecommerce.product.dto.ShippingTemplateRuleDTO;
import com.ecommerce.product.entity.ShippingTemplate;
import com.ecommerce.product.entity.ShippingTemplateRule;
import com.ecommerce.product.mapper.ShippingTemplateMapper;
import com.ecommerce.product.mapper.ShippingTemplateRuleMapper;
import com.ecommerce.product.service.ShippingTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingTemplateServiceImpl implements ShippingTemplateService {

    private final ShippingTemplateMapper templateMapper;
    private final ShippingTemplateRuleMapper ruleMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<ShippingTemplateDTO> page(Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<ShippingTemplate> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ShippingTemplate::getStatus, status);
        }
        wrapper.orderByDesc(ShippingTemplate::getCreateTime);
        Page<ShippingTemplate> page = templateMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ShippingTemplateDTO> list = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public ShippingTemplateDTO getDetail(Long id) {
        ShippingTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "运费模板不存在");
        }
        return toDTOWithRules(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ShippingTemplateCreateRequest request) {
        ShippingTemplate template = ShippingTemplate.builder()
                .name(request.getName())
                .chargeType(request.getChargeType())
                .defaultFee(request.getDefaultFee())
                .freeThreshold(request.getFreeThreshold())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .build();
        templateMapper.insert(template);
        saveRules(template.getId(), request.getRules());
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ShippingTemplateCreateRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "模板ID不能为空");
        }
        ShippingTemplate existing = templateMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "运费模板不存在");
        }
        existing.setName(request.getName());
        existing.setChargeType(request.getChargeType());
        existing.setDefaultFee(request.getDefaultFee());
        existing.setFreeThreshold(request.getFreeThreshold());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        templateMapper.updateById(existing);

        ruleMapper.delete(new LambdaQueryWrapper<ShippingTemplateRule>()
                .eq(ShippingTemplateRule::getTemplateId, request.getId()));
        saveRules(request.getId(), request.getRules());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        templateMapper.deleteById(id);
        ruleMapper.delete(new LambdaQueryWrapper<ShippingTemplateRule>()
                .eq(ShippingTemplateRule::getTemplateId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ShippingTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "运费模板不存在");
        }
        template.setStatus(status);
        templateMapper.updateById(template);
    }

    @Override
    public ShippingFeeCalculateResult calculateFee(Long templateId, String regionCode, BigDecimal quantity) {
        ShippingTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            return ShippingFeeCalculateResult.builder()
                    .shippingFee(BigDecimal.ZERO).freeShipping(true).templateName("默认").build();
        }

        if (template.getFreeThreshold() != null && quantity.compareTo(template.getFreeThreshold()) >= 0) {
            return ShippingFeeCalculateResult.builder()
                    .shippingFee(BigDecimal.ZERO).freeShipping(true).templateName(template.getName()).build();
        }

        LambdaQueryWrapper<ShippingTemplateRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(ShippingTemplateRule::getTemplateId, templateId);
        List<ShippingTemplateRule> rules = ruleMapper.selectList(ruleWrapper);

        ShippingTemplateRule matched = rules.stream()
                .filter(r -> "ALL".equals(r.getRegionCodes()) ||
                        (regionCode != null && r.getRegionCodes().contains(regionCode)))
                .findFirst()
                .orElse(null);

        BigDecimal fee;
        if (matched == null) {
            fee = template.getDefaultFee();
        } else {
            fee = matched.getStartFee();
            BigDecimal remaining = quantity.subtract(matched.getStartThreshold());
            if (remaining.compareTo(BigDecimal.ZERO) > 0 && matched.getAdditionalThreshold().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal extraUnits = remaining.divide(matched.getAdditionalThreshold(), 0, RoundingMode.CEILING);
                fee = fee.add(extraUnits.multiply(matched.getAdditionalFee()));
            }
        }

        return ShippingFeeCalculateResult.builder()
                .shippingFee(fee).freeShipping(false).templateName(template.getName()).build();
    }

    private void saveRules(Long templateId, List<ShippingTemplateCreateRequest.RuleItem> items) {
        if (items == null || items.isEmpty()) return;
        for (var item : items) {
            ShippingTemplateRule rule = ShippingTemplateRule.builder()
                    .templateId(templateId)
                    .regionCodes(item.getRegionCodes() != null ? item.getRegionCodes() : "ALL")
                    .regionNames(item.getRegionNames() != null ? item.getRegionNames() : "全国")
                    .startThreshold(item.getStartThreshold() != null ? item.getStartThreshold() : BigDecimal.ONE)
                    .startFee(item.getStartFee() != null ? item.getStartFee() : BigDecimal.ZERO)
                    .additionalThreshold(item.getAdditionalThreshold() != null ? item.getAdditionalThreshold() : BigDecimal.ONE)
                    .additionalFee(item.getAdditionalFee() != null ? item.getAdditionalFee() : BigDecimal.ZERO)
                    .build();
            ruleMapper.insert(rule);
        }
    }

    private ShippingTemplateDTO toDTO(ShippingTemplate t) {
        return ShippingTemplateDTO.builder()
                .id(t.getId()).name(t.getName()).chargeType(t.getChargeType())
                .defaultFee(t.getDefaultFee()).freeThreshold(t.getFreeThreshold())
                .status(t.getStatus()).createTime(t.getCreateTime().format(FMT))
                .build();
    }

    private ShippingTemplateDTO toDTOWithRules(ShippingTemplate t) {
        ShippingTemplateDTO dto = toDTO(t);
        List<ShippingTemplateRule> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<ShippingTemplateRule>().eq(ShippingTemplateRule::getTemplateId, t.getId()));
        dto.setRules(rules.stream().map(r -> ShippingTemplateRuleDTO.builder()
                .id(r.getId()).templateId(r.getTemplateId())
                .regionCodes(r.getRegionCodes()).regionNames(r.getRegionNames())
                .startThreshold(r.getStartThreshold()).startFee(r.getStartFee())
                .additionalThreshold(r.getAdditionalThreshold()).additionalFee(r.getAdditionalFee())
                .build()).toList());
        return dto;
    }
}
