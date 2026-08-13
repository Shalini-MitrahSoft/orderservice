package com.example.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class InventoryServiceException extends RuntimeException {

    public InventoryServiceException(String message) {
        super(message);
    }

    public InventoryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
