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
import java.util.List;
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

    private volatile WebSocketClient webSocketClient;
    private volatile boolean shuttingDown = false;
    private volatile long reconnectDelaySeconds = 5;

    private final ScheduledExecutorService reconnectScheduler =
            Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void connect() {
        reconnectDelaySeconds = 5;
        connectSingle();
    }

    private void connectSingle() {
        if (shuttingDown) return;

        List<String> symbols = List.of(finnhubConfig.getStockSymbolsArray());
        log.info("Connecting to Finnhub WebSocket for {} symbols...", symbols.size());

        try {
            String url = finnhubConfig.getWebsocketUrl() + "?token=" + finnhubConfig.getApiKey();

            final WebSocketClient[] holder = new WebSocketClient[1];

            holder[0] = new WebSocketClient(new URI(url)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("Connected to Finnhub WebSocket — subscribing to {} symbols", symbols.size());
                    reconnectDelaySeconds = 5; // reset backoff on successful connect
                    for (String symbol : symbols) {
                        send("{\"type\":\"subscribe\",\"symbol\":\"" + symbol + "\"}");
                        log.debug("Subscribed to: {}", symbol);
                    }
                    log.info("All {} symbols subscribed on single connection", symbols.size());
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("Finnhub WebSocket closed: {} - {}", code, reason);
                    if (!shuttingDown) {
                        if (reason != null && reason.contains("429")) {
                            log.warn("Rate-limited (429) — waiting 60s before reconnect");
                            scheduleReconnect(60);
                        } else {
                            scheduleReconnect(reconnectDelaySeconds);
                            reconnectDelaySeconds = Math.min(reconnectDelaySeconds * 2, 60);
                        }
                    }
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Finnhub WebSocket error: {}", ex.getMessage());
                }
            };

            webSocketClient = holder[0];
            webSocketClient.connect();

        } catch (Exception e) {
            log.error("Failed to connect to Finnhub: {}", e.getMessage());
            scheduleReconnect(reconnectDelaySeconds);
            reconnectDelaySeconds = Math.min(reconnectDelaySeconds * 2, 60);
        }
    }

    private void handleMessage(String message) {
        try {
            FinnhubTradeDto tradeDto = objectMapper.readValue(message, FinnhubTradeDto.class);

            if (!"trade".equals(tradeDto.getType()) || tradeDto.getData() == null) {
                return;
            }

            for (FinnhubTradeDto.TradeData trade : tradeDto.getData()) {
                StockDataDto stockDataDto = stockDataService.updateStockPrice(
                        trade.getSymbol(),
                        trade.getPrice(),
                        trade.getVolume().longValue()
                );
                stockDataPublisher.publish(stockDataDto);
            }
        } catch (Exception e) {
            log.error("Error handling Finnhub message: {}", e.getMessage());
        }
    }

    private void scheduleReconnect(long delaySeconds) {
        log.info("Scheduling reconnect in {}s...", delaySeconds);
        reconnectScheduler.schedule(this::connectSingle, delaySeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void disconnect() {
        shuttingDown = true;
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }
        reconnectScheduler.shutdown();
    }
}