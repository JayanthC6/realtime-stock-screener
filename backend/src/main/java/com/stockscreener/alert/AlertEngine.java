package com.stockscreener.alert;

import com.stockscreener.dto.StockDataDto;
import com.stockscreener.model.AlertHistory;
import com.stockscreener.model.AlertRule;
import com.stockscreener.model.ConditionType;
import com.stockscreener.model.IndicatorType;
import com.stockscreener.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEngine {

    private final AlertService alertService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.stockscreener.service.EmailService emailService;

    public void evaluate(StockDataDto stockDataDto) {
        // Get all active rules for this symbol
        List<AlertRule> activeRules = alertService.getAllActiveRules()
                .stream()
                .filter(rule -> rule.getSymbol()
                        .equals(stockDataDto.getSymbol()))
                .toList();

        for (AlertRule rule : activeRules) {
            Double currentValue = extractValue(stockDataDto, rule.getIndicatorType());

            if (currentValue == null) continue;

            boolean triggered = isTriggered(currentValue,
                    rule.getConditionType(), rule.getThresholdValue());

            if (triggered) {
                String message = buildAlertMessage(rule, currentValue);

                // Save alert history to PostgreSQL
                AlertHistory history = AlertHistory.builder()
                        .user(rule.getUser())
                        .alertRule(rule)
                        .symbol(rule.getSymbol())
                        .indicatorType(rule.getIndicatorType())
                        .triggeredValue(currentValue)
                        .thresholdValue(rule.getThresholdValue())
                        .message(message)
                        .build();

                alertService.saveAlertHistory(history);

                // Push instant notification to user via WebSocket
                messagingTemplate.convertAndSendToUser(
                        rule.getUser().getEmail(),
                        "/queue/alerts",
                        message
                );

                // Send email notification
                String subject = "FinVeda Alert: " + rule.getSymbol();
                emailService.sendAlertEmail(rule.getUser().getEmail(), subject, message);

                log.info("Alert triggered: {}", message);
            }
        }
    }

    private Double extractValue(StockDataDto dto, IndicatorType indicatorType) {
        return switch (indicatorType) {
            case PE_RATIO -> dto.getPeRatio();
            case RSI -> dto.getRsi();
            case VOLUME -> dto.getVolume() != null
                    ? dto.getVolume().doubleValue() : null;
        };
    }

    private boolean isTriggered(Double currentValue,
            ConditionType conditionType, Double threshold) {
        return switch (conditionType) {
            case GREATER_THAN -> currentValue > threshold;
            case LESS_THAN -> currentValue < threshold;
        };
    }

    private String buildAlertMessage(AlertRule rule, Double currentValue) {
        String condition = rule.getConditionType() == ConditionType.GREATER_THAN
                ? "exceeded" : "dropped below";
        return String.format("ALERT: %s %s %s threshold of %.2f. Current value: %.2f",
                rule.getSymbol(),
                rule.getIndicatorType().name(),
                condition,
                rule.getThresholdValue(),
                currentValue);
    }
}