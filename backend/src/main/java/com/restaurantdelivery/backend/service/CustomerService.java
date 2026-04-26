package com.restaurantdelivery.backend.service;

import com.restaurantdelivery.backend.api.dto.CustomerCreateRequest;
import com.restaurantdelivery.backend.api.dto.CustomerCreateResponse;
import com.restaurantdelivery.backend.persistence.entity.CustomerEntity;
import com.restaurantdelivery.backend.persistence.repository.CustomerRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerCreateResponse createCustomer(CustomerCreateRequest request) {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(UUID.randomUUID().toString());
        customer.setName(request.name().trim());
        customer.setEmail(request.email().trim());
        customer.setPhone(request.phone().trim());
        customer.setAddress(request.address().trim());

        CustomerEntity saved = customerRepository.save(customer);
        return new CustomerCreateResponse(saved.getId(), request, saved.getCreatedAt().toString());
    }
}
