package com.restaurantdelivery.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ManualStatusUpdateRequest(@NotBlank String status) {
}
