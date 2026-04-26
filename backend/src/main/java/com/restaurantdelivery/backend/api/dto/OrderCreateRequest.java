package com.restaurantdelivery.backend.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderCreateRequest(
    @NotBlank String customerId,
    @NotEmpty List<@Valid PizzaSelectionRequest> pizzas
) {
}
