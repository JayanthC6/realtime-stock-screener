package com.stockscreener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class FinnhubConfig {

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.websocket.url}")
    private String websocketUrl;

    @Value("${screener.stocks}")
    private String stockSymbols;

    @Value("${screener.refresh.interval}")
    private long refreshInterval;

    public String[] getStockSymbolsArray() {
        return stockSymbols.split(",");
    }
}