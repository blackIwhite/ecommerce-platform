package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.order.dto.PaymentDTO;
import com.ecommerce.order.entity.Payment;
import com.ecommerce.order.mapper.PaymentMapper;
import com.ecommerce.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentDTO createPayment(Long orderId, BigDecimal amount) {
        String paymentNo = generatePaymentNo();

        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentNo(paymentNo)
                .amount(amount)
                .payMethod("MOCK")
                .status(1)
                .payTime(LocalDateTime.now())
                .build();
        paymentMapper.insert(payment);

        log.info("Mock payment created: paymentNo={}, orderId={}, amount={}", paymentNo, orderId, amount);
        return toDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId)
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1");
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Payment not found for order: " + orderId);
        }
        return toDTO(payment);
    }

    private String generatePaymentNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "PAY" + datePart + random;
    }

    private PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .paymentNo(payment.getPaymentNo())
                .amount(payment.getAmount())
                .payMethod(payment.getPayMethod())
                .status(payment.getStatus())
                .payTime(payment.getPayTime())
                .createTime(payment.getCreateTime())
                .build();
    }
}
