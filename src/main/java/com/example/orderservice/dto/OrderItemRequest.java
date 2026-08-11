package com.example.orderservice.dto;


import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {

    private Long productId;

    private Integer quantity;

    private BigDecimal unitPrice;
}
