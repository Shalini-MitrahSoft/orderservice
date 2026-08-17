package com.example.orderservice.client;

import com.example.orderservice.dto.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CUSTOMER-SERVICE", fallback = CustomerClientFallback.class)
public interface CustomerClient {

    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomer(
            @PathVariable("id") Long id
    );
}