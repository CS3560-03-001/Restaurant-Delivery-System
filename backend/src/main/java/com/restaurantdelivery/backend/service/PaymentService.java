package com.restaurantdelivery.backend.service;

import com.restaurantdelivery.backend.api.dto.PaymentRequest;
import com.restaurantdelivery.backend.api.dto.PaymentResponse;
import com.restaurantdelivery.backend.domain.OrderStatus;
import com.restaurantdelivery.backend.exception.BadRequestException;
import com.restaurantdelivery.backend.exception.InvalidStateException;
import com.restaurantdelivery.backend.persistence.entity.OrderEntity;
import com.restaurantdelivery.backend.persistence.entity.PaymentEntity;
import com.restaurantdelivery.backend.persistence.repository.OrderRepository;
import com.restaurantdelivery.backend.persistence.repository.PaymentRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(OrderService orderService, OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse submitPayment(PaymentRequest request) {
        OrderEntity order = orderService.loadOrder(request.orderId());

        if (order.getPayment() != null) {
            throw new InvalidStateException("Payment already exists for order: " + request.orderId());
        }

        if (request.amount().compareTo(order.getAmount()) != 0) {
            throw new BadRequestException(
                "Payment amount must match the persisted order total of " + order.getAmount().setScale(2)
            );
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrder(order);
        payment.setPaymentMethod(request.paymentMethod().trim());
        payment.setBillingName(request.billingName().trim());
        payment.setCardLast4(lastFourDigits(request.cardLast4()));
        payment.setAmount(request.amount().setScale(2));
        payment.setStatus("Accepted");

        PaymentEntity saved = paymentRepository.save(payment);
        order.setPayment(saved);
        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);

        return new PaymentResponse(saved.getId(), order.getId(), saved.getStatus(), saved.getAmount(), saved.getPaidAt().toString());
    }

    private String lastFourDigits(String rawValue) {
        String trimmed = rawValue.trim();
        if (trimmed.length() != 4 || !trimmed.chars().allMatch(Character::isDigit)) {
            throw new BadRequestException("cardLast4 must contain exactly 4 digits");
        }
        return trimmed;
    }
}
