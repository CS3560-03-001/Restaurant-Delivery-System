package com.restaurantdelivery.backend.api.dto;

public record OrderStatusResponse(
    String orderId,
    String status,
    int etaMinutes,
    String etaLabel,
    DriverInfoResponse driver,
    String updatedAt
) {
}
