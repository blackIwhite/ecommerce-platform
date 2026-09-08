package com.ecommerce.order.service;

import com.ecommerce.order.dto.PaymentDTO;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentDTO createPayment(Long orderId, BigDecimal amount);

    PaymentDTO getPaymentByOrderId(Long orderId);
}
