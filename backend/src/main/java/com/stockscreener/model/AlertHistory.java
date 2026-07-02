package com.stockscreener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_rule_id", nullable = false)
    private AlertRule alertRule;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    private IndicatorType indicatorType;

    @Column(nullable = false)
    private Double triggeredValue;

    @Column(nullable = false)
    private Double thresholdValue;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    private String message;

    @PrePersist
    public void prePersist() {
        triggeredAt = LocalDateTime.now();
    }
}