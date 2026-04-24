package com.restaurantdelivery.backend.api;

import com.restaurantdelivery.backend.api.dto.OrderCreateRequest;
import com.restaurantdelivery.backend.api.dto.OrderCreateResponse;
import com.restaurantdelivery.backend.api.dto.OrderStatusResponse;
import com.restaurantdelivery.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}/status")
    public OrderStatusResponse getStatus(@PathVariable String orderId) {
        return orderService.getOrderStatus(orderId);
    }
}
