package com.restaurantdelivery.backend.domain;

public enum OrderStatus {
    AWAITING_PAYMENT("Awaiting Payment"),
    PREPARING("Preparing"),
    BAKING("Baking"),
    READY_FOR_DELIVERY("Ready for Delivery"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
