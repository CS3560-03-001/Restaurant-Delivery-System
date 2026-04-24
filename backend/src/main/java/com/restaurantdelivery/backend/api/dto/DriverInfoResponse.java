package com.restaurantdelivery.backend.api.dto;

public record DriverInfoResponse(
    String name,
    String phone,
    String vehicle
) {
}
