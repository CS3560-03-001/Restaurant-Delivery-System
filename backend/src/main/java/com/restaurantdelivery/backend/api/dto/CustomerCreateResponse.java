package com.restaurantdelivery.backend.api.dto;

public record CustomerCreateResponse(
    String customerId,
    CustomerCreateRequest profile,
    String createdAt
) {
}
