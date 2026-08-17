package com.example.orderservice.client;

import com.example.orderservice.dto.ProductResponse;
import com.example.orderservice.exception.InvalidOrderException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(
            @Qualifier("loadBalancedWebClientBuilder")
            WebClient.Builder webClientBuilder) {

        this.webClient = webClientBuilder.build();
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "productFallback"
    )
    public ProductResponse getProduct(Long productId) {
        try {
            ProductResponse product = webClient
                    .get()
                    .uri(
                        "http://INVENTORY-SERVICE/api/products/{productId}",
                        productId
                    )
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();

            if (product == null || product.getPrice() == null) {
                throw new InvalidOrderException(
                        "Price not available for product: " + productId
                );
            }

            return product;

        } catch (WebClientException exception) {

            if (exception instanceof WebClientResponseException.NotFound) {

                throw new InvalidOrderException(
                        "Product not found: " + productId
                );
            }

            if (exception instanceof WebClientResponseException responseException) {

                throw new InvalidOrderException(
                        "Inventory Service returned status: "
                                + responseException.getStatusCode()
                );
            }

            throw new InvalidOrderException(
                    "Unable to get product price from Inventory Service"
            );
        }
    }
    public ProductResponse productFallback(
            Long productId,
            Throwable throwable) {

        throw new InvalidOrderException(
                "Product Service is currently unavailable for product: "
                        + productId
        );
    }
}