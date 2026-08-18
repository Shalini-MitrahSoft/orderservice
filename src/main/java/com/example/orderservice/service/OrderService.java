package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import java.util.List;

public interface OrderService {


    OrderResponse createOrder(OrderRequest request);

    OrderResponse cancelOrder(Long id);

    OrderResponse getOrder(Long id);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByCustomer(Long customerId);

    String getOrderStatus(Long id);


}
