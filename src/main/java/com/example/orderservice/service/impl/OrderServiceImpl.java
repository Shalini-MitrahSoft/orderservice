package com.example.orderservice.service.impl;

import com.example.orderservice.client.InventoryClient;
import com.example.orderservice.constants.OrderStatus;
import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.exception.InvalidOrderException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @Override
    public OrderResponse createOrder(OrderRequest request) {

        Order order = new Order();

        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("At least one order item is required");
        }

        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null
                    || itemRequest.getQuantity() == null
                    || itemRequest.getUnitPrice() == null) {
                throw new InvalidOrderException("productId, quantity, and unitPrice are required");
            }

            if (itemRequest.getQuantity() <= 0) {
                throw new InvalidOrderException("quantity must be greater than zero");
            }

            InventoryResponse inventory = inventoryClient.getInventory(itemRequest.getProductId());

            if (inventory.getAvailableQuantity() < itemRequest.getQuantity()) {
                order.setStatus(OrderStatus.REJECTED);
                order.setItems(new ArrayList<>());
                order.setTotalAmount(BigDecimal.ZERO);

                Order rejectedOrder = orderRepository.save(order);
                return toResponse(rejectedOrder);
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            BigDecimal itemTotal = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setTotalPrice(itemTotal);

            orderItems.add(item);
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CONFIRMED);

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse cancelOrder(Long id) {

        Order order = findOrder(id);
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);
    }

    @Override
    public OrderResponse getOrder(Long id) {

        Order order = findOrder(id);
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {

        List<Order> orders = orderRepository.findByCustomerId(customerId);
        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(toResponse(order));
        }

        return responses;
    }

    @Override
    public String getOrderStatus(Long id) {

        return findOrder(id).getStatus().name();
    }

    private Order findOrder(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponse toResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        return response;
    }
}
