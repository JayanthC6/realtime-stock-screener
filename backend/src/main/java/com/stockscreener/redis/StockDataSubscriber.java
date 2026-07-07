package com.stockscreener.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockscreener.dto.StockDataDto;
import com.stockscreener.alert.AlertEngine;
import com.stockscreener.screening.ScreeningEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDataSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScreeningEngine screeningEngine;
    private final AlertEngine alertEngine;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            StockDataDto stockDataDto = objectMapper.readValue(body, StockDataDto.class);

            // Push live price update to React frontend via WebSocket
            messagingTemplate.convertAndSend("/topic/stocks", stockDataDto);

            // Run screening engine
            screeningEngine.screen(stockDataDto);

            // Run alert engine
            alertEngine.evaluate(stockDataDto);

            log.debug("Processed stock update for: {}", stockDataDto.getSymbol());
        } catch (JsonProcessingException e) {
            log.error("Error processing Redis message: {}", e.getMessage());
        }
    }
}