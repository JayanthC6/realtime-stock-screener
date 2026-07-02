package com.stockscreener.controller;

import com.stockscreener.dto.AlertHistoryDto;
import com.stockscreener.dto.AlertRuleRequest;
import com.stockscreener.dto.AlertRuleResponse;
import com.stockscreener.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertRuleResponse> createAlertRule(
            @Valid @RequestBody AlertRuleRequest request) {
        return ResponseEntity.ok(alertService.createAlertRule(request));
    }

    @GetMapping
    public ResponseEntity<List<AlertRuleResponse>> getUserAlertRules() {
        return ResponseEntity.ok(alertService.getUserAlertRules());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable Long id) {
        alertService.deleteAlertRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<AlertHistoryDto>> getUserAlertHistory() {
        return ResponseEntity.ok(alertService.getUserAlertHistory());
    }
}