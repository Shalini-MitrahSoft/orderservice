package com.example.orderservice.dto;


import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {

    private Long productId;

    private Integer quantity;

    private BigDecimal unitPrice;
}
