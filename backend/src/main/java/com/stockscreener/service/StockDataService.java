package com.stockscreener.service;

import com.stockscreener.dto.StockDataDto;
import com.stockscreener.model.StockData;
import com.stockscreener.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataService {

    private final StockDataRepository stockDataRepository;

    // In-memory price history for RSI calculation (last 14 prices per symbol)
    private final Map<String, List<Double>> priceHistory = new ConcurrentHashMap<>();

    @Transactional
    public StockDataDto updateStockPrice(String symbol, Double price, Long volume) {
        // Update price history for RSI
        priceHistory.computeIfAbsent(symbol, k -> new ArrayList<>());
        List<Double> history = priceHistory.get(symbol);
        history.add(price);

        // Keep only last 15 prices for RSI(14) calculation
        if (history.size() > 15) {
            history.remove(0);
        }

        // Calculate RSI
        Double rsi = calculateRSI(history);

        // Get or create stock data record
        StockData stockData = stockDataRepository.findBySymbol(symbol)
                .orElse(StockData.builder()
                        .symbol(symbol)
                        .build());

        // Calculate price change
        Double previousClose = stockData.getCurrentPrice() != null
                ? stockData.getCurrentPrice() : price;
        Double priceChange = price - previousClose;
        Double priceChangePercent = previousClose != 0
                ? (priceChange / previousClose) * 100 : 0.0;

        // Update stock data
        stockData.setCurrentPrice(price);
        stockData.setPreviousClose(previousClose);
        stockData.setPriceChange(priceChange);
        stockData.setPriceChangePercent(priceChangePercent);
        stockData.setVolume(volume);
        stockData.setRsi(rsi);

        stockDataRepository.save(stockData);

        return mapToDto(stockData);
    }

    public List<StockDataDto> getAllStocks() {
        return stockDataRepository.findAllByOrderBySymbolAsc()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public StockDataDto getStockBySymbol(String symbol) {
        StockData stockData = stockDataRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException(
                        "Stock not found: " + symbol));
        return mapToDto(stockData);
    }

    private Double calculateRSI(List<Double> prices) {
        if (prices.size() < 2) return null;

        double gains = 0.0;
        double losses = 0.0;
        int periods = Math.min(prices.size() - 1, 14);

        for (int i = prices.size() - periods; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gains += change;
            } else {
                losses += Math.abs(change);
            }
        }

        if (losses == 0) return 100.0;

        double avgGain = gains / periods;
        double avgLoss = losses / periods;
        double rs = avgGain / avgLoss;

        return 100 - (100 / (1 + rs));
    }

    private StockDataDto mapToDto(StockData stockData) {
        return StockDataDto.builder()
                .symbol(stockData.getSymbol())
                .currentPrice(stockData.getCurrentPrice())
                .previousClose(stockData.getPreviousClose())
                .priceChange(stockData.getPriceChange())
                .priceChangePercent(stockData.getPriceChangePercent())
                .volume(stockData.getVolume())
                .peRatio(stockData.getPeRatio())
                .rsi(stockData.getRsi())
                .high(stockData.getHigh())
                .low(stockData.getLow())
                .open(stockData.getOpen())
                .lastUpdated(stockData.getLastUpdated())
                .build();
    }
}