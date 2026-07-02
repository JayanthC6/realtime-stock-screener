package com.stockscreener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenerResultDto {
    private String symbol;
    private Double currentPrice;
    private Double peRatio;
    private Double rsi;
    private Long volume;
    private Boolean peAlert;
    private Boolean rsiOverbought;
    private Boolean rsiOversold;
    private Boolean volumeSpike;
}