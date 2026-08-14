package com.example.orderservice.service.impl;

import com.example.orderservice.client.CustomerClient;
import com.example.orderservice.constants.OrderStatus;
import com.example.orderservice.dto.CustomerResponse;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.exception.InvalidOrderException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final CustomerClient customerClient;

    @Override
    public OrderResponse createOrder(OrderRequest request) {

        if (request.getCustomerId() == null) {

            log.warn("Order creation failed: customerId is null");

            throw new InvalidOrderException("Customer ID is required");
        }

        Long customerId = request.getCustomerId();

        log.info("Starting customer validation for customerId={}", customerId);

        CustomerResponse customer;

        try {
            log.info("Calling Customer Service for customerId={}", customerId);

            customer = customerClient.getCustomer(customerId);

            log.info("Customer Service call successful for customerId={}", customerId);

        } catch (FeignException.NotFound exception) {

            log.warn("Customer not found for customerId={}, status={}", customerId, exception.status());

            throw new InvalidOrderException("Invalid customer ID: " + customerId);

        } catch (FeignException exception) {

            log.error(
                    "Customer Service call failed for customerId={}, status={}, error={}",
                    customerId,
                    exception.status(),
                    exception.getMessage(),
                    exception
            );

            throw new InvalidOrderException("Unable to validate customer. Customer Service status: " + exception.status());

        } catch (Exception exception) {

            log.error("Unexpected error while validating customerId={}", customerId, exception);
            throw new InvalidOrderException("Unexpected error while validating customer");
        }

        if (customer == null || customer.getId() == null) {

            log.warn("Customer Service returned an invalid response for customerId={}", customerId);
            throw new InvalidOrderException("Invalid customer ID: " + customerId);
        }

        if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {

            log.warn(
                    "Order creation rejected because customerId={} has status={}",
                    customerId,
                    customer.getStatus()
            );

            throw new InvalidOrderException("Customer with ID " + customerId + " is not ACTIVE");
        }

        log.info("Customer validation completed successfully for customerId={}", customerId);

        Order order = new Order();

        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("At least one order item is required");
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null
                    || itemRequest.getQuantity() == null) {
                throw new InvalidOrderException("productId and quantity are required");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());


            orderItems.add(item);
        }

        order.setItems(orderItems);
        order.setTotalAmount(null);

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
