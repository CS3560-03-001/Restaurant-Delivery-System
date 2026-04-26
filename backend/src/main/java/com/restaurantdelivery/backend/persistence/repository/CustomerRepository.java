package com.restaurantdelivery.backend.persistence.repository;

import com.restaurantdelivery.backend.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {
}
