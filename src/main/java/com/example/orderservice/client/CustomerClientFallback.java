package com.example.orderservice.client;

import com.example.orderservice.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerClientFallback
        implements CustomerClient {

    @Override
    public CustomerResponse getCustomer(Long id) {
        throw new RuntimeException(
                "Customer Service is currently unavailable"
        );
    }
}