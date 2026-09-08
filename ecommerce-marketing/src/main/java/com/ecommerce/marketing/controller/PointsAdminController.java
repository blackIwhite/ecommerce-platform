package com.ecommerce.marketing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.marketing.entity.PointsRule;
import com.ecommerce.marketing.mapper.PointsRuleMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marketing/admin/points")
@RequiredArgsConstructor
@Tag(name = "积分管理(后台)", description = "Points admin APIs")
public class PointsAdminController {

    private final PointsRuleMapper pointsRuleMapper;

    @GetMapping("/rules")
    public Result<List<PointsRule>> listRules() {
        List<PointsRule> rules = pointsRuleMapper.selectList(
                new LambdaQueryWrapper<PointsRule>().orderByAsc(PointsRule::getId));
        return Result.success(rules);
    }

    @PutMapping("/rules/{id}")
    public Result<Void> updateRule(@PathVariable Long id, @RequestBody PointsRule rule) {
        PointsRule existing = pointsRuleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.POINTS_RULE_NOT_FOUND);
        }
        existing.setRuleValue(rule.getRuleValue());
        existing.setDescription(rule.getDescription());
        existing.setStatus(rule.getStatus());
        pointsRuleMapper.updateById(existing);
        return Result.success();
    }
}
