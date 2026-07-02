package com.stockscreener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
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

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}