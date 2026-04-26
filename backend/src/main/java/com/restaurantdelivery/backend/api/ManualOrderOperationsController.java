package com.restaurantdelivery.backend.api;

import com.restaurantdelivery.backend.api.dto.DeliveryAssignmentRequest;
import com.restaurantdelivery.backend.api.dto.ManualStatusUpdateRequest;
import com.restaurantdelivery.backend.api.dto.OrderStatusResponse;
import com.restaurantdelivery.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class ManualOrderOperationsController {

    private final OrderService orderService;

    public ManualOrderOperationsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PatchMapping("/{orderId}/status")
    public OrderStatusResponse updateStatus(
        @PathVariable String orderId,
        @Valid @RequestBody ManualStatusUpdateRequest request
    ) {
        return orderService.updateStatus(orderId, request.status());
    }

    @PutMapping("/{orderId}/delivery-assignment")
    public OrderStatusResponse assignDriver(
        @PathVariable String orderId,
        @Valid @RequestBody DeliveryAssignmentRequest request
    ) {
        return orderService.assignDriver(orderId, request);
    }
}
