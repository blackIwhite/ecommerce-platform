package com.ecommerce.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.marketing.dto.PointsAccountDTO;
import com.ecommerce.api.marketing.dto.PointsEarnRequest;
import com.ecommerce.api.marketing.dto.PointsLogDTO;
import com.ecommerce.api.marketing.dto.PointsRedeemRequest;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.marketing.entity.PointsAccount;
import com.ecommerce.marketing.entity.PointsLog;
import com.ecommerce.marketing.mapper.PointsAccountMapper;
import com.ecommerce.marketing.mapper.PointsLogMapper;
import com.ecommerce.marketing.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final PointsAccountMapper pointsAccountMapper;
    private final PointsLogMapper pointsLogMapper;

    @Override
    public PointsAccountDTO getAccount(Long userId) {
        PointsAccount account = getOrCreateAccount(userId);
        return toAccountDTO(account);
    }

    @Override
    public PageResult<PointsLogDTO> getLogs(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;

        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId);
        if (type != null) {
            wrapper.eq(PointsLog::getType, type);
        }
        wrapper.orderByDesc(PointsLog::getId);

        Page<PointsLog> page = pointsLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<PointsLogDTO> records = page.getRecords().stream().map(this::toLogDTO).toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void earnPoints(PointsEarnRequest request) {
        if (request.getPoints() == null || request.getPoints() <= 0) {
            return;
        }

        PointsAccount account = getOrCreateAccount(request.getUserId());
        account.setTotalPoints(account.getTotalPoints() + request.getPoints());
        account.setAvailablePoints(account.getAvailablePoints() + request.getPoints());
        account.setLevel(calculateLevel(account.getTotalPoints()));
        pointsAccountMapper.updateById(account);

        PointsLog pointsLog = PointsLog.builder()
                .userId(request.getUserId())
                .type(1)
                .points(request.getPoints())
                .balance(account.getAvailablePoints())
                .source(request.getSource())
                .referenceId(request.getReferenceId())
                .description(request.getDescription())
                .build();
        pointsLogMapper.insert(pointsLog);

        log.info("Earned {} points for user {}, source: {}", request.getPoints(), request.getUserId(), request.getSource());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redeemPoints(PointsRedeemRequest request) {
        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getUserId, request.getUserId()));

        if (account == null || account.getAvailablePoints() < request.getPoints()) {
            throw new BusinessException(ResultCode.POINTS_INSUFFICIENT);
        }

        account.setAvailablePoints(account.getAvailablePoints() - request.getPoints());
        account.setUsedPoints(account.getUsedPoints() + request.getPoints());
        pointsAccountMapper.updateById(account);

        PointsLog pointsLog = PointsLog.builder()
                .userId(request.getUserId())
                .type(2)
                .points(request.getPoints())
                .balance(account.getAvailablePoints())
                .source("order")
                .referenceId(request.getOrderId())
                .description(request.getDescription())
                .build();
        pointsLogMapper.insert(pointsLog);

        log.info("Redeemed {} points for user {}, orderId: {}", request.getPoints(), request.getUserId(), request.getOrderId());
    }

    private PointsAccount getOrCreateAccount(Long userId) {
        PointsAccount account = pointsAccountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccount>().eq(PointsAccount::getUserId, userId));
        if (account == null) {
            account = PointsAccount.builder()
                    .userId(userId)
                    .totalPoints(0)
                    .availablePoints(0)
                    .usedPoints(0)
                    .expiredPoints(0)
                    .level(1)
                    .build();
            pointsAccountMapper.insert(account);
        }
        return account;
    }

    private int calculateLevel(int totalPoints) {
        if (totalPoints >= 20000) return 4;
        if (totalPoints >= 5000) return 3;
        if (totalPoints >= 1000) return 2;
        return 1;
    }

    private PointsAccountDTO toAccountDTO(PointsAccount account) {
        PointsAccountDTO dto = new PointsAccountDTO();
        dto.setUserId(account.getUserId());
        dto.setTotalPoints(account.getTotalPoints());
        dto.setAvailablePoints(account.getAvailablePoints());
        dto.setUsedPoints(account.getUsedPoints());
        dto.setExpiredPoints(account.getExpiredPoints());
        dto.setLevel(account.getLevel());
        dto.setLevelName(switch (account.getLevel()) {
            case 4 -> "钻石会员";
            case 3 -> "金卡会员";
            case 2 -> "银卡会员";
            default -> "普通用户";
        });
        return dto;
    }

    private PointsLogDTO toLogDTO(PointsLog log) {
        PointsLogDTO dto = new PointsLogDTO();
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setType(log.getType());
        dto.setTypeName(switch (log.getType()) {
            case 1 -> "获得";
            case 2 -> "消费";
            case 3 -> "过期";
            default -> "未知";
        });
        dto.setPoints(log.getPoints());
        dto.setBalance(log.getBalance());
        dto.setSource(log.getSource());
        dto.setReferenceId(log.getReferenceId());
        dto.setDescription(log.getDescription());
        dto.setCreateTime(log.getCreateTime() != null
                ? log.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null);
        return dto;
    }
}
