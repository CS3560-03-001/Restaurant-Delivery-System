package com.restaurantdelivery.backend.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
    @NotBlank String orderId,
    @NotBlank String paymentMethod,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
    @NotBlank String billingName,
    @NotBlank String cardLast4
) {
}
