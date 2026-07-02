package com.stockscreener.dto;

import com.stockscreener.model.ConditionType;
import com.stockscreener.model.IndicatorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertRuleRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Indicator type is required")
    private IndicatorType indicatorType;

    @NotNull(message = "Condition type is required")
    private ConditionType conditionType;

    @NotNull(message = "Threshold value is required")
    private Double thresholdValue;
}