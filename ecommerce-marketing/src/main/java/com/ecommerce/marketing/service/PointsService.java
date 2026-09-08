package com.ecommerce.marketing.service;

import com.ecommerce.api.marketing.dto.PointsAccountDTO;
import com.ecommerce.api.marketing.dto.PointsLogDTO;
import com.ecommerce.api.marketing.dto.PointsEarnRequest;
import com.ecommerce.api.marketing.dto.PointsRedeemRequest;
import com.ecommerce.common.core.page.PageResult;

public interface PointsService {

    PointsAccountDTO getAccount(Long userId);

    PageResult<PointsLogDTO> getLogs(Long userId, Integer type, Integer pageNum, Integer pageSize);

    void earnPoints(PointsEarnRequest request);

    void redeemPoints(PointsRedeemRequest request);
}
