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
public class PriceHistoryDto {
    private Double price;
    private Long volume;
    private LocalDateTime timestamp;
}
