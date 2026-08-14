package com.example.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventType;

    private Long orderId;

    private Long customerId;

    private String status;

    private String reason;

    private String timestamp;
}