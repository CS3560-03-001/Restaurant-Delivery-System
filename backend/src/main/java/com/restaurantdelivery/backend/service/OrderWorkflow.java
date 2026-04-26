package com.restaurantdelivery.backend.service;

import com.restaurantdelivery.backend.domain.OrderStatus;
import com.restaurantdelivery.backend.exception.BadRequestException;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OrderWorkflow {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.PREPARING, Set.of(OrderStatus.BAKING),
        OrderStatus.BAKING, Set.of(OrderStatus.READY_FOR_DELIVERY),
        OrderStatus.READY_FOR_DELIVERY, Set.of(OrderStatus.OUT_FOR_DELIVERY),
        OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED, Set.of(),
        OrderStatus.AWAITING_PAYMENT, Set.of(OrderStatus.PREPARING)
    );

    public OrderStatus parseManualStatus(String rawStatus) {
        String normalized = rawStatus.trim().toUpperCase().replace(' ', '_');
        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unknown order status: " + rawStatus);
        }
    }

    public boolean canTransition(OrderStatus from, OrderStatus to) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }
}
