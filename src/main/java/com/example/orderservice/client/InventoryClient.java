package com.example.orderservice.client;

import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.exception.InventoryNotFoundException;
import com.example.orderservice.exception.InventoryServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class InventoryClient {

    private static final String INVENTORY_URL =
            "http://INVENTORY-SERVICE/api/inventory/{productId}";

    private final RestClient restClient;

    public InventoryClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @CircuitBreaker(
            name = "inventoryService",
            fallbackMethod = "inventoryFallback"
    )
    public InventoryResponse getInventory(Long productId) {
        try {
            InventoryResponse response = restClient.get()
                    .uri(INVENTORY_URL, productId)
                    .retrieve()
                    .body(InventoryResponse.class);

            if (response == null || response.getAvailableQuantity() == null) {
                throw new InventoryServiceException("Inventory Service returned no inventory data for product " + productId);
            }

            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new InventoryNotFoundException(productId, exception);
            }

            throw new InventoryServiceException("Inventory Service request failed for product " + productId,exception);
        } catch (RestClientException exception) {
            throw new InventoryServiceException(
                    "Inventory Service is unavailable for product " + productId,
                    exception);
        }
    }

    public InventoryResponse inventoryFallback(
            Long productId,
            Throwable throwable) {

        throw new InventoryServiceException(
                "Inventory Service is currently unavailable for product "
                        + productId,
                throwable
        );
    }
}
