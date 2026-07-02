package com.stockscreener.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockscreener.config.FinnhubConfig;
import com.stockscreener.dto.FinnhubTradeDto;
import com.stockscreener.dto.StockDataDto;
import com.stockscreener.redis.StockDataPublisher;
import com.stockscreener.service.StockDataService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinnhubWebSocketClient {

    private final FinnhubConfig finnhubConfig;
    private final ObjectMapper objectMapper;
    private final StockDataPublisher stockDataPublisher;
    private final StockDataService stockDataService;

    private WebSocketClient webSocketClient;
    private final ScheduledExecutorService reconnectScheduler =
            Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void connect() {
        initializeAndConnect();
    }

    private void initializeAndConnect() {
        try {
            String url = finnhubConfig.getWebsocketUrl()
                    + "?token=" + finnhubConfig.getApiKey();

            webSocketClient = new WebSocketClient(new URI(url)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("Connected to Finnhub WebSocket");
                    subscribeToStocks();
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("Finnhub WebSocket closed: {} - {}", code, reason);
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Finnhub WebSocket error: {}", ex.getMessage());
                }
            };

            webSocketClient.connect();
        } catch (Exception e) {
            log.error("Failed to connect to Finnhub: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private void subscribeToStocks() {
        String[] symbols = finnhubConfig.getStockSymbolsArray();
        for (String symbol : symbols) {
            String subscribeMessage = "{\"type\":\"subscribe\",\"symbol\":\"" + symbol + "\"}";
            webSocketClient.send(subscribeMessage);
            log.debug("Subscribed to: {}", symbol);
        }
        log.info("Subscribed to {} stocks", symbols.length);
    }

    private void handleMessage(String message) {
        try {
            FinnhubTradeDto tradeDto = objectMapper.readValue(message, FinnhubTradeDto.class);

            if (!"trade".equals(tradeDto.getType()) || tradeDto.getData() == null) {
                return;
            }

            for (FinnhubTradeDto.TradeData trade : tradeDto.getData()) {
                // Update stock data in DB and get enriched DTO
                StockDataDto stockDataDto = stockDataService.updateStockPrice(
                        trade.getSymbol(),
                        trade.getPrice(),
                        trade.getVolume().longValue()
                );

                // Publish to Redis Pub/Sub
                stockDataPublisher.publish(stockDataDto);
            }
        } catch (Exception e) {
            log.error("Error handling Finnhub message: {}", e.getMessage());
        }
    }

    private void scheduleReconnect() {
        log.info("Scheduling reconnect in 5 seconds...");
        reconnectScheduler.schedule(this::initializeAndConnect, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void disconnect() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            // Unsubscribe from all stocks before closing
            for (String symbol : finnhubConfig.getStockSymbolsArray()) {
                String unsubscribeMessage = "{\"type\":\"unsubscribe\",\"symbol\":\""
                        + symbol + "\"}";
                webSocketClient.send(unsubscribeMessage);
            }
            webSocketClient.close();
            log.info("Disconnected from Finnhub WebSocket");
        }
        reconnectScheduler.shutdown();
    }
}