package com.stockscreener.service;

import com.stockscreener.exception.ResourceNotFoundException;
import com.stockscreener.model.PriceHistory;
import com.stockscreener.model.StockData;
import com.stockscreener.repository.PriceHistoryRepository;
import com.stockscreener.repository.StockDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockDataService Unit Tests")
class StockDataServiceTest {

    @Mock private StockDataRepository stockDataRepository;
    @Mock private PriceHistoryRepository priceHistoryRepository;

    @InjectMocks private StockDataService service;

    // ── RSI calculation tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("RSI returns null when fewer than 2 prices provided")
    void testRsi_insufficientData_returnsNull() {
        assertThat(service.calculateRSI(Collections.emptyList())).isNull();
        assertThat(service.calculateRSI(List.of(100.0))).isNull();
    }

    @Test
    @DisplayName("RSI returns 100 when there are only gains and no losses")
    void testRsi_onlyGains_returns100() {
        List<Double> prices = Arrays.asList(100.0, 101.0, 102.0, 103.0, 104.0);
        Double rsi = service.calculateRSI(prices);
        assertThat(rsi).isEqualTo(100.0);
    }

    @Test
    @DisplayName("RSI is between 0 and 100 for a mixed price series")
    void testRsi_mixedPrices_inValidRange() {
        List<Double> prices = Arrays.asList(
            44.34, 44.09, 44.15, 43.61, 44.33,
            44.83, 45.10, 45.15, 43.61, 44.33,
            44.83, 45.10, 45.15, 43.61, 44.33
        );
        Double rsi = service.calculateRSI(prices);
        assertThat(rsi).isNotNull()
                       .isBetween(0.0, 100.0);
    }

    @Test
    @DisplayName("RSI null when prices list is null")
    void testRsi_nullInput_returnsNull() {
        assertThat(service.calculateRSI(null)).isNull();
    }

    // ── updateStockPrice tests ────────────────────────────────────────────────

    @Test
    @DisplayName("updateStockPrice: new symbol creates a new StockData record")
    void testUpdateStockPrice_newSymbol_createsRecord() {
        // Arrange
        String symbol = "AAPL";
        Double price  = 150.0;
        Long volume   = 1000L;

        when(priceHistoryRepository.save(any(PriceHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(priceHistoryRepository.findTop15BySymbolOrderByTimestampDesc(symbol))
                .thenReturn(List.of(priceHistory(symbol, price)));
        when(stockDataRepository.findBySymbol(symbol)).thenReturn(Optional.empty());
        when(stockDataRepository.save(any(StockData.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        var dto = service.updateStockPrice(symbol, price, volume);

        // Assert
        assertThat(dto.getSymbol()).isEqualTo(symbol);
        assertThat(dto.getCurrentPrice()).isEqualTo(price);
        verify(stockDataRepository).save(any(StockData.class));
        verify(priceHistoryRepository).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("updateStockPrice: calculates priceChange correctly for existing symbol")
    void testUpdateStockPrice_existingSymbol_calculatesPriceChange() {
        // Arrange
        String symbol       = "MSFT";
        Double previousPrice = 300.0;
        Double newPrice      = 315.0;

        StockData existing = StockData.builder()
                .symbol(symbol).currentPrice(previousPrice)
                .high(300.0).low(298.0).open(298.0)
                .build();

        when(priceHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(priceHistoryRepository.findTop15BySymbolOrderByTimestampDesc(symbol))
                .thenReturn(List.of(priceHistory(symbol, previousPrice), priceHistory(symbol, newPrice)));
        when(stockDataRepository.findBySymbol(symbol)).thenReturn(Optional.of(existing));
        when(stockDataRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var dto = service.updateStockPrice(symbol, newPrice, 500L);

        // Assert
        assertThat(dto.getPriceChange()).isEqualTo(15.0);
        assertThat(dto.getPriceChangePercent()).isCloseTo(5.0, within(0.001));
        assertThat(dto.getHigh()).isEqualTo(315.0); // high should update
    }

    @Test
    @DisplayName("getStockBySymbol: throws ResourceNotFoundException for unknown symbol")
    void testGetStockBySymbol_unknownSymbol_throwsException() {
        when(stockDataRepository.findBySymbol("UNKNOWN")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getStockBySymbol("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    @DisplayName("getStockHistory: throws ResourceNotFoundException for unknown symbol")
    void testGetStockHistory_unknownSymbol_throwsException() {
        when(stockDataRepository.existsBySymbol("FAKE")).thenReturn(false);
        assertThatThrownBy(() -> service.getStockHistory("FAKE", 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private PriceHistory priceHistory(String symbol, double price) {
        return PriceHistory.builder()
                .symbol(symbol).price(price)
                .volume(100L).timestamp(LocalDateTime.now())
                .build();
    }
}
