package com.restaurantdelivery.backend.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreateResponse(
    String orderId,
    String customerId,
    List<PizzaSelectionRequest> pizzas,
    BigDecimal amount,
    String currency,
    String status,
    boolean checkoutReady,
    String createdAt
) {
}
