package com.stockscreener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDataDto {
    private String symbol;
    private Double currentPrice;
    private Double previousClose;
    private Double priceChange;
    private Double priceChangePercent;
    private Long volume;
    private Double peRatio;
    private Double rsi;
    private Double high;
    private Double low;
    private Double open;
    private LocalDateTime lastUpdated;
}