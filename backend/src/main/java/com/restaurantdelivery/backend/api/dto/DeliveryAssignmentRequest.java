package com.restaurantdelivery.backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DeliveryAssignmentRequest(
    @NotBlank String name,
    @NotBlank String phone,
    @NotBlank String vehicle,
    @Min(0) int etaMinutes
) {
}
