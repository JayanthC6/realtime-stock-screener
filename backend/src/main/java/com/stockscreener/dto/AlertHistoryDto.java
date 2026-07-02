package com.stockscreener.dto;

import com.stockscreener.model.IndicatorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistoryDto {
    private Long id;
    private String symbol;
    private IndicatorType indicatorType;
    private Double triggeredValue;
    private Double thresholdValue;
    private LocalDateTime triggeredAt;
    private String message;
}