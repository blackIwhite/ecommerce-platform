package com.ecommerce.marketing.task;

import com.ecommerce.marketing.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponScheduledTask {

    private final CouponService couponService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void expireUnusedCoupons() {
        int count = couponService.expireUnusedCoupons();
        if (count > 0) {
            log.info("Expired {} unused coupons", count);
        }
    }
}
