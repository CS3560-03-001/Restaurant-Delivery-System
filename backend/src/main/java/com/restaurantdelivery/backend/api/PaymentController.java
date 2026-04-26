package com.restaurantdelivery.backend.api;

import com.restaurantdelivery.backend.api.dto.PaymentRequest;
import com.restaurantdelivery.backend.api.dto.PaymentResponse;
import com.restaurantdelivery.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse submitPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.submitPayment(request);
    }
}
