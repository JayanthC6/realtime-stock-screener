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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinnhubWebSocketClient {

    private static final int MAX_SYMBOLS_PER_CONNECTION = 20;

    private final FinnhubConfig finnhubConfig;
    private final ObjectMapper objectMapper;
    private final StockDataPublisher stockDataPublisher;
    private final StockDataService stockDataService;

    private final List<WebSocketClient> webSocketClients = new CopyOnWriteArrayList<>();
    private volatile boolean shuttingDown;
    private final ScheduledExecutorService reconnectScheduler =
            Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void connect() {
        List<String> symbols = List.of(finnhubConfig.getStockSymbolsArray());
        for (int i = 0; i < symbols.size(); i += MAX_SYMBOLS_PER_CONNECTION) {
            connectBatch(new ArrayList<>(symbols.subList(i, Math.min(i + MAX_SYMBOLS_PER_CONNECTION, symbols.size()))));
        }
    }

    private void connectBatch(List<String> symbolsBatch) {
        try {
            String url = finnhubConfig.getWebsocketUrl()
                    + "?token=" + finnhubConfig.getApiKey();

            final WebSocketClient[] clientHolder = new WebSocketClient[1];

            clientHolder[0] = new WebSocketClient(new URI(url)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("Connected to Finnhub WebSocket");
                    subscribeToStocks(clientHolder[0], symbolsBatch);
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("Finnhub WebSocket closed: {} - {}", code, reason);
                    if (!shuttingDown && (reason == null || !reason.contains("429"))) {
                        scheduleReconnect(symbolsBatch);
                    }
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Finnhub WebSocket error: {}", ex.getMessage());
                }
            };

            webSocketClients.add(clientHolder[0]);
            clientHolder[0].connect();
        } catch (Exception e) {
            log.error("Failed to connect to Finnhub: {}", e.getMessage());
            scheduleReconnect(symbolsBatch);
        }
    }

    private void subscribeToStocks(WebSocketClient client, List<String> symbols) {
        for (String symbol : symbols) {
            String subscribeMessage = "{\"type\":\"subscribe\",\"symbol\":\"" + symbol + "\"}";
            client.send(subscribeMessage);
            log.debug("Subscribed to: {}", symbol);
        }
        log.info("Subscribed to {} stocks in batch", symbols.size());
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

    private void scheduleReconnect(List<String> symbolsBatch) {
        log.info("Scheduling reconnect in 5 seconds...");
        reconnectScheduler.schedule(() -> connectBatch(new ArrayList<>(symbolsBatch)), 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void disconnect() {
        shuttingDown = true;
        for (WebSocketClient webSocketClient : webSocketClients) {
            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.close();
            }
        }
        reconnectScheduler.shutdown();
    }
}