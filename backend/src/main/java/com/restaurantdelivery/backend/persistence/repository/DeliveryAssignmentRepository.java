package com.restaurantdelivery.backend.persistence.repository;

import com.restaurantdelivery.backend.persistence.entity.DeliveryAssignmentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignmentEntity, Long> {
    Optional<DeliveryAssignmentEntity> findByOrder_Id(String orderId);
}
