package com.restaurantdelivery.backend.service;

import com.restaurantdelivery.backend.api.dto.DeliveryAssignmentRequest;
import com.restaurantdelivery.backend.api.dto.DriverInfoResponse;
import com.restaurantdelivery.backend.api.dto.OrderCreateRequest;
import com.restaurantdelivery.backend.api.dto.OrderCreateResponse;
import com.restaurantdelivery.backend.api.dto.OrderStatusResponse;
import com.restaurantdelivery.backend.api.dto.PizzaSelectionRequest;
import com.restaurantdelivery.backend.domain.OrderStatus;
import com.restaurantdelivery.backend.domain.StaffRole;
import com.restaurantdelivery.backend.exception.BadRequestException;
import com.restaurantdelivery.backend.exception.InvalidStateException;
import com.restaurantdelivery.backend.exception.NotFoundException;
import com.restaurantdelivery.backend.persistence.entity.CustomerEntity;
import com.restaurantdelivery.backend.persistence.entity.DeliveryAssignmentEntity;
import com.restaurantdelivery.backend.persistence.entity.OrderEntity;
import com.restaurantdelivery.backend.persistence.entity.OrderItemEntity;
import com.restaurantdelivery.backend.persistence.entity.OrderItemToppingEntity;
import com.restaurantdelivery.backend.persistence.entity.StaffMemberEntity;
import com.restaurantdelivery.backend.persistence.repository.CustomerRepository;
import com.restaurantdelivery.backend.persistence.repository.DeliveryAssignmentRepository;
import com.restaurantdelivery.backend.persistence.repository.OrderRepository;
import com.restaurantdelivery.backend.persistence.repository.StaffMemberRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final MenuPricingService menuPricingService;
    private final OrderWorkflow orderWorkflow;

    public OrderService(
        CustomerRepository customerRepository,
        OrderRepository orderRepository,
        DeliveryAssignmentRepository deliveryAssignmentRepository,
        StaffMemberRepository staffMemberRepository,
        MenuPricingService menuPricingService,
        OrderWorkflow orderWorkflow
    ) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.menuPricingService = menuPricingService;
        this.orderWorkflow = orderWorkflow;
    }

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new NotFoundException("Customer not found: " + request.customerId()));

        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID().toString());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setCurrency("USD");

        BigDecimal total = BigDecimal.ZERO;
        List<PizzaSelectionRequest> pizzas = new ArrayList<>();

        for (int index = 0; index < request.pizzas().size(); index++) {
            PizzaSelectionRequest pizza = request.pizzas().get(index);
            BigDecimal lineTotal = menuPricingService.pricePizza(pizza);
            total = total.add(lineTotal);
            pizzas.add(new PizzaSelectionRequest(pizza.crust(), pizza.sauce(), pizza.cheese(), pizza.toppingsOrEmpty()));

            OrderItemEntity item = new OrderItemEntity();
            item.setLineNumber(index + 1);
            item.setCrustOptionId(pizza.crust());
            item.setSauceOptionId(pizza.sauce());
            item.setCheeseOptionId(pizza.cheese());
            item.setLineTotal(lineTotal);

            for (String topping : pizza.toppingsOrEmpty()) {
                OrderItemToppingEntity toppingEntity = new OrderItemToppingEntity();
                toppingEntity.setToppingOptionId(topping);
                item.addTopping(toppingEntity);
            }

            order.addItem(item);
        }

        order.setAmount(total);
        OrderEntity saved = orderRepository.save(order);
        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(String orderId) {
        OrderEntity order = loadOrder(orderId);
        int etaMinutes = switch (order.getStatus()) {
            case AWAITING_PAYMENT -> 0;
            case PREPARING -> 28;
            case BAKING -> 20;
            case READY_FOR_DELIVERY -> 12;
            case OUT_FOR_DELIVERY -> order.getDeliveryAssignment() == null ? 8 : order.getDeliveryAssignment().getEtaMinutes();
            case DELIVERED -> 0;
        };

        String etaLabel = order.getStatus() == OrderStatus.AWAITING_PAYMENT
            ? "Payment required before preparation"
            : order.getStatus() == OrderStatus.DELIVERED ? "Delivered" : etaMinutes + " minutes";

        DriverInfoResponse driver = order.getDeliveryAssignment() == null ? null : new DriverInfoResponse(
            order.getDeliveryAssignment().getDriverName(),
            order.getDeliveryAssignment().getDriverPhone(),
            order.getDeliveryAssignment().getDriverVehicle()
        );

        return new OrderStatusResponse(
            order.getId(),
            order.getStatus().label(),
            etaMinutes,
            etaLabel,
            driver,
            order.getUpdatedAt().toString()
        );
    }

    @Transactional
    public OrderStatusResponse updateStatus(String orderId, String requestedStatus) {
        OrderEntity order = loadOrder(orderId);
        OrderStatus nextStatus = orderWorkflow.parseManualStatus(requestedStatus);

        if (!orderWorkflow.canTransition(order.getStatus(), nextStatus)) {
            throw new InvalidStateException(
                "Cannot transition order from %s to %s".formatted(order.getStatus().label(), nextStatus.label())
            );
        }

        order.setStatus(nextStatus);
        return getOrderStatus(orderRepository.save(order).getId());
    }

    @Transactional
    public OrderStatusResponse assignDriver(String orderId, DeliveryAssignmentRequest request) {
        DriverAssignmentDetails driverDetails = resolveDriverDetails(request);
        OrderEntity order = loadOrder(orderId);
        DeliveryAssignmentEntity assignment = order.getDeliveryAssignment();

        if (assignment == null) {
            assignment = new DeliveryAssignmentEntity();
            assignment.setOrder(order);
            order.setDeliveryAssignment(assignment);
        }

        assignment.setDriverName(driverDetails.name());
        assignment.setDriverPhone(driverDetails.phone());
        assignment.setDriverVehicle(driverDetails.vehicle());
        assignment.setEtaMinutes(request.etaMinutes());

        deliveryAssignmentRepository.save(assignment);
        if (order.getStatus() == OrderStatus.READY_FOR_DELIVERY) {
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            orderRepository.save(order);
        }

        return getOrderStatus(orderId);
    }

    private DriverAssignmentDetails resolveDriverDetails(DeliveryAssignmentRequest request) {
        if (hasText(request.demoDriverId())) {
            StaffMemberEntity demoDriver = staffMemberRepository.findByDemoIdAndRoleAndActiveTrue(
                    request.demoDriverId().trim(),
                    StaffRole.DRIVER
                )
                .orElseThrow(() -> new BadRequestException("Demo driver not found or inactive: " + request.demoDriverId().trim()));

            return new DriverAssignmentDetails(
                demoDriver.getDisplayName(),
                demoDriver.getPhone(),
                demoDriver.getVehicleDescription()
            );
        }

        return new DriverAssignmentDetails(
            requireText(request.name(), "Driver name is required"),
            requireText(request.phone(), "Driver phone is required"),
            requireText(request.vehicle(), "Driver vehicle is required")
        );
    }

    private String requireText(String value, String message) {
        if (!hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Transactional(readOnly = true)
    public OrderEntity loadOrder(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    public OrderCreateResponse toOrderResponse(OrderEntity order) {
        List<PizzaSelectionRequest> pizzas = order.getItems().stream()
            .sorted((left, right) -> Integer.compare(left.getLineNumber(), right.getLineNumber()))
            .map(item -> new PizzaSelectionRequest(
                item.getCrustOptionId(),
                item.getSauceOptionId(),
                item.getCheeseOptionId(),
                item.getToppings().stream().map(OrderItemToppingEntity::getToppingOptionId).toList()
            ))
            .toList();

        return new OrderCreateResponse(
            order.getId(),
            order.getCustomer().getId(),
            pizzas,
            order.getAmount(),
            order.getCurrency(),
            order.getStatus().label(),
            !pizzas.isEmpty(),
            order.getCreatedAt().toString()
        );
    }

    private record DriverAssignmentDetails(String name, String phone, String vehicle) {
    }
}
