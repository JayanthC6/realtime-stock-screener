package com.stockscreener.service;

import com.stockscreener.dto.AlertHistoryDto;
import com.stockscreener.dto.AlertRuleRequest;
import com.stockscreener.dto.AlertRuleResponse;
import com.stockscreener.model.AlertHistory;
import com.stockscreener.model.AlertRule;
import com.stockscreener.model.User;
import com.stockscreener.repository.AlertHistoryRepository;
import com.stockscreener.repository.AlertRuleRepository;
import com.stockscreener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertHistoryRepository alertHistoryRepository;
    private final UserRepository userRepository;

    public AlertRuleResponse createAlertRule(AlertRuleRequest request) {
        User user = getCurrentUser();

        AlertRule alertRule = AlertRule.builder()
                .user(user)
                .symbol(request.getSymbol().toUpperCase())
                .indicatorType(request.getIndicatorType())
                .conditionType(request.getConditionType())
                .thresholdValue(request.getThresholdValue())
                .build();

        AlertRule saved = alertRuleRepository.save(alertRule);
        log.info("Alert rule created for user: {} symbol: {}",
                user.getEmail(), request.getSymbol());

        return mapToAlertRuleResponse(saved);
    }

    public List<AlertRuleResponse> getUserAlertRules() {
        User user = getCurrentUser();
        return alertRuleRepository.findByUserAndActive(user, true)
                .stream()
                .map(this::mapToAlertRuleResponse)
                .toList();
    }

    @Transactional
    public void deleteAlertRule(Long id) {
        User user = getCurrentUser();
        AlertRule alertRule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert rule not found"));

        if (!alertRule.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this alert rule");
        }

        alertRule.setActive(false);
        alertRuleRepository.save(alertRule);
        log.info("Alert rule {} deactivated", id);
    }

    public List<AlertHistoryDto> getUserAlertHistory() {
        User user = getCurrentUser();
        return alertHistoryRepository.findByUserOrderByTriggeredAtDesc(user)
                .stream()
                .map(this::mapToAlertHistoryDto)
                .toList();
    }

    public void saveAlertHistory(AlertHistory alertHistory) {
        alertHistoryRepository.save(alertHistory);
    }

    public List<AlertRule> getAllActiveRules() {
        return alertRuleRepository.findAllByActive(true);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AlertRuleResponse mapToAlertRuleResponse(AlertRule alertRule) {
        return AlertRuleResponse.builder()
                .id(alertRule.getId())
                .symbol(alertRule.getSymbol())
                .indicatorType(alertRule.getIndicatorType())
                .conditionType(alertRule.getConditionType())
                .thresholdValue(alertRule.getThresholdValue())
                .active(alertRule.getActive())
                .createdAt(alertRule.getCreatedAt())
                .build();
    }

    private AlertHistoryDto mapToAlertHistoryDto(AlertHistory alertHistory) {
        return AlertHistoryDto.builder()
                .id(alertHistory.getId())
                .symbol(alertHistory.getSymbol())
                .indicatorType(alertHistory.getIndicatorType())
                .triggeredValue(alertHistory.getTriggeredValue())
                .thresholdValue(alertHistory.getThresholdValue())
                .triggeredAt(alertHistory.getTriggeredAt())
                .message(alertHistory.getMessage())
                .build();
    }
}