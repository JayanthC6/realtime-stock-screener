package com.stockscreener.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockscreener.dto.StockDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDataPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(StockDataDto stockDataDto) {
        try {
            String message = objectMapper.writeValueAsString(stockDataDto);
            redisTemplate.convertAndSend("stock-updates", message);
            log.debug("Published stock data to Redis: {}", stockDataDto.getSymbol());
        } catch (JsonProcessingException e) {
            log.error("Error publishing stock data to Redis: {}", e.getMessage());
        }
    }
}