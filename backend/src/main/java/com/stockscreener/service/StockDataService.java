package com.stockscreener.service;

import com.stockscreener.dto.PriceHistoryDto;
import com.stockscreener.dto.StockDataDto;
import com.stockscreener.exception.ResourceNotFoundException;
import com.stockscreener.model.PriceHistory;
import com.stockscreener.model.StockData;
import com.stockscreener.repository.PriceHistoryRepository;
import com.stockscreener.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataService {

    private final StockDataRepository stockDataRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // ── Update price on every Finnhub tick ────────────────────────────────────

    @Transactional
    @CachePut(value = "stocks", key = "#symbol")
    public StockDataDto updateStockPrice(String symbol, Double price, Long volume) {

        // 1. Persist raw tick to price_history table
        priceHistoryRepository.save(
                PriceHistory.builder()
                        .symbol(symbol)
                        .price(price)
                        .volume(volume)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // 2. Load last 15 prices from DB for accurate RSI (survives restarts)
        List<PriceHistory> recentHistory = new java.util.ArrayList<>(
                priceHistoryRepository.findTop15BySymbolOrderByTimestampDesc(symbol)
        );

        // Reverse so oldest → newest for RSI calculation
        Collections.reverse(recentHistory);
        List<Double> prices = recentHistory.stream()
                .map(PriceHistory::getPrice)
                .toList();

        Double rsi = calculateRSI(prices);

        // 3. Get or create the snapshot record in stock_data
        StockData stockData = stockDataRepository.findBySymbol(symbol)
                .orElse(StockData.builder()
                        .symbol(symbol)
                        .open(price)
                        .high(price)
                        .low(price)
                        .build());

        // 4. Calculate price change vs previous snapshot
        Double previousClose = stockData.getCurrentPrice() != null
                ? stockData.getCurrentPrice() : price;
        Double priceChange = price - previousClose;
        Double priceChangePercent = previousClose != 0
                ? (priceChange / previousClose) * 100 : 0.0;

        // 5. Track intraday high / low
        if (stockData.getHigh() == null || price > stockData.getHigh()) {
            stockData.setHigh(price);
        }
        if (stockData.getLow() == null || price < stockData.getLow()) {
            stockData.setLow(price);
        }
        if (stockData.getPeRatio() == null) {
            // Mock P/E between 10 and 40 for screener demonstration
            stockData.setPeRatio(10.0 + Math.random() * 30.0);
        }

        // 6. Update snapshot
        stockData.setCurrentPrice(price);
        stockData.setPreviousClose(previousClose);
        stockData.setPriceChange(priceChange);
        stockData.setPriceChangePercent(priceChangePercent);
        stockData.setVolume(volume);
        stockData.setRsi(rsi);

        stockDataRepository.save(stockData);
        log.debug("Updated {} → price={} rsi={}", symbol, price, rsi);

        return mapToDto(stockData);
    }

    // ── Read all stocks (paginated) ───────────────────────────────────────────

    public Page<StockDataDto> getAllStocks(Pageable pageable) {
        return stockDataRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    /** Non-paginated version kept for backward compatibility */
    public List<StockDataDto> getAllStocks() {
        return stockDataRepository.findAllByOrderBySymbolAsc()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ── Read single stock (cached) ────────────────────────────────────────────

    @Cacheable(value = "stocks", key = "#symbol")
    public StockDataDto getStockBySymbol(String symbol) {
        StockData stockData = stockDataRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "symbol", symbol));
        return mapToDto(stockData);
    }

    // ── Price history for chart endpoint ─────────────────────────────────────

    public List<PriceHistoryDto> getStockHistory(String symbol, int hours) {
        // Verify the symbol exists
        if (!stockDataRepository.existsBySymbol(symbol)) {
            throw new ResourceNotFoundException("Stock", "symbol", symbol);
        }

        LocalDateTime from = LocalDateTime.now().minusHours(hours);
        LocalDateTime to   = LocalDateTime.now();

        return priceHistoryRepository
                .findBySymbolAndTimestampBetweenOrderByTimestampAsc(symbol, from, to)
                .stream()
                .map(ph -> PriceHistoryDto.builder()
                        .price(ph.getPrice())
                        .volume(ph.getVolume())
                        .timestamp(ph.getTimestamp())
                        .build())
                .toList();
    }

    // ── RSI calculation (Wilder's smoothed method) ───────────────────────────

    /**
     * Computes RSI-14 using Wilder's smoothing.
     * Requires at least 2 data points; returns null if insufficient.
     */
    Double calculateRSI(List<Double> prices) {
        if (prices == null || prices.size() < 2) return null;

        int periods = Math.min(prices.size() - 1, 14);
        double gains = 0.0;
        double losses = 0.0;

        // First average (simple mean of first 'periods' changes)
        for (int i = prices.size() - periods; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) gains  += change;
            else            losses += Math.abs(change);
        }

        if (periods == 0) return null;

        double avgGain = gains  / periods;
        double avgLoss = losses / periods;

        if (avgLoss == 0) return 100.0;

        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    // ── DTO mapping ───────────────────────────────────────────────────────────

    private StockDataDto mapToDto(StockData s) {
        return StockDataDto.builder()
                .symbol(s.getSymbol())
                .currentPrice(s.getCurrentPrice())
                .previousClose(s.getPreviousClose())
                .priceChange(s.getPriceChange())
                .priceChangePercent(s.getPriceChangePercent())
                .volume(s.getVolume())
                .peRatio(s.getPeRatio())
                .rsi(s.getRsi())
                .high(s.getHigh())
                .low(s.getLow())
                .open(s.getOpen())
                .lastUpdated(s.getLastUpdated())
                .build();
    }
}