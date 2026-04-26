package com.restaurantdelivery.backend.api.dto;

import jakarta.validation.constraints.Min;

public record DeliveryAssignmentRequest(
    String demoDriverId,
    String name,
    String phone,
    String vehicle,
    @Min(0) int etaMinutes
) {
}
