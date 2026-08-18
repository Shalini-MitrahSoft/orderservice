package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {

        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {

        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> getOrders(@RequestParam(required = false) Long customerId) {

        if (customerId == null) {
            return orderService.getAllOrders();
        }
        return orderService.getOrdersByCustomer(customerId);
    }

    @GetMapping("/{id}/status")
    public String getStatus(@PathVariable Long id) {

        return orderService.getOrderStatus(id);
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {

        return orderService.cancelOrder(id);
    }
}
