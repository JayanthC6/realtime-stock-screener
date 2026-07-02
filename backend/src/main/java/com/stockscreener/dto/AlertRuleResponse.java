package com.stockscreener.dto;

import com.stockscreener.model.ConditionType;
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
public class AlertRuleResponse {
    private Long id;
    private String symbol;
    private IndicatorType indicatorType;
    private ConditionType conditionType;
    private Double thresholdValue;
    private Boolean active;
    private LocalDateTime createdAt;
}