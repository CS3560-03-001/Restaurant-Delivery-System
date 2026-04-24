package com.restaurantdelivery.backend.persistence.repository;

import com.restaurantdelivery.backend.persistence.entity.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByOrder_Id(String orderId);
}
