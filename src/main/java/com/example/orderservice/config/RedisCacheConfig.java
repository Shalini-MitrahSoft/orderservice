package com.example.orderservice.config;

import com.example.orderservice.dto.OrderResponse;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     ObjectMapper objectMapper,
                                     @Value("${spring.cache.redis.time-to-live:10m}") Duration ttl) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues();

        JacksonJsonRedisSerializer<OrderResponse> orderSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, OrderResponse.class);
        JacksonJsonRedisSerializer<List<OrderResponse>> orderListSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, OrderResponse.class));
        JacksonJsonRedisSerializer<String> stringSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, String.class);
        JacksonJsonRedisSerializer<Boolean> booleanSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, Boolean.class);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(Map.of(
                        "order", typed(base, orderSerializer),
                        "orderList", typed(base, orderListSerializer),
                        "customerOrders", typed(base, orderListSerializer),
                        "orderStatus", typed(base, stringSerializer),
                        "orderExists", typed(base, booleanSerializer)))
                .build();
    }

    private <T> RedisCacheConfiguration typed(RedisCacheConfiguration base,
                                                JacksonJsonRedisSerializer<T> serializer) {
        return base.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
