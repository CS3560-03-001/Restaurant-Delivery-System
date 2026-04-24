package com.restaurantdelivery.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PizzaSelectionRequest(
    @NotBlank String crust,
    @NotBlank String sauce,
    @NotBlank String cheese,
    List<String> toppings
) {
    public List<String> toppingsOrEmpty() {
        return toppings == null ? List.of() : toppings;
    }
}
