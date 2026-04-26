package com.restaurantdelivery.backend.api.dto;

import java.math.BigDecimal;

public record PaymentResponse(
    String paymentId,
    String orderId,
    String status,
    BigDecimal amount,
    String paidAt
) {
}
