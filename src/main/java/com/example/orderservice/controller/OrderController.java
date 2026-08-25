package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkOrderExists(@PathVariable Long id) {

        if (orderService.existsById(id)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> getAllowedMethods(@PathVariable Long id) {

        Set<HttpMethod> allowedMethods = Set.of(
                HttpMethod.GET,
                HttpMethod.HEAD,
                HttpMethod.PUT,
                HttpMethod.OPTIONS
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.ALLOW,
                        allowedMethods.stream()
                                .map(HttpMethod::name)
                                .reduce((first, second) -> first + ", " + second)
                                .orElse("")
                )
                .build();
    }
}
