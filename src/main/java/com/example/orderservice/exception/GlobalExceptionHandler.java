package com.example.orderservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<Map<String, Object>> handleInventoryServiceException(
            InventoryServiceException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory Service Unavailable",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInventoryNotFoundException(
            InventoryNotFoundException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Inventory Not Found",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        return ResponseEntity.status(status).body(body);
    }
}
