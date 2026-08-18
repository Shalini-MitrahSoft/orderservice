package com.example.orderservice.kafka;

import com.example.orderservice.event.OrderEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${app.kafka.order-topic}")
    private String orderTopic;

    public void publishOrderEvent(OrderEvent event) {

        try {
            String eventJson = objectMapper.writeValueAsString(event);

            String key = String.valueOf(event.getOrderId());

            log.info("Publishing order event: topic={}, orderId={}, eventType={}",
                    orderTopic, event.getOrderId(), event.getEventType());

            kafkaTemplate
                    .send(orderTopic, key, eventJson)
                    .whenComplete((result, exception) -> {

                        if (exception != null) {
                            log.error("Failed to publish order event: orderId={}", event.getOrderId(), exception);
                            return;
                        }

                        log.info("Order event published: orderId={}, partition={}, offset={}", event.getOrderId(), result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    });

        } catch (JsonProcessingException exception) {

            log.error("Failed to convert OrderEvent to JSON: orderId={}", event.getOrderId(), exception);

            throw new IllegalStateException("Failed to prepare order event", exception
            );
        }
    }
}