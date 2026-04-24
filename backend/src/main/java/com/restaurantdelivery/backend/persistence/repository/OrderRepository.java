package com.restaurantdelivery.backend.persistence.repository;

import com.restaurantdelivery.backend.persistence.entity.OrderEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    @EntityGraph(attributePaths = {"customer", "items", "items.toppings", "payment", "deliveryAssignment"})
    Optional<OrderEntity> findById(String id);
}
