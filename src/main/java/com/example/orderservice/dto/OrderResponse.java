package com.example.orderservice.dto;

import com.example.orderservice.constants.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {

    private Long id;

    private Long customerId;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;
}