package com.stockscreener.controller;

import com.stockscreener.dto.ScreenerResultDto;
import com.stockscreener.dto.StockDataDto;
import com.stockscreener.screening.ScreeningEngine;
import com.stockscreener.service.StockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final StockDataService stockDataService;
    private final ScreeningEngine screeningEngine;

    @GetMapping
    public ResponseEntity<List<StockDataDto>> getAllStocks() {
        return ResponseEntity.ok(stockDataService.getAllStocks());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockDataDto> getStockBySymbol(
            @PathVariable String symbol) {
        return ResponseEntity.ok(
                stockDataService.getStockBySymbol(symbol.toUpperCase()));
    }

    @GetMapping("/screener/results")
    public ResponseEntity<List<ScreenerResultDto>> getScreenerResults() {
        return ResponseEntity.ok(screeningEngine.getAllScreenerResults());
    }

    @GetMapping("/screener/triggered")
    public ResponseEntity<List<ScreenerResultDto>> getTriggeredResults() {
        return ResponseEntity.ok(screeningEngine.getTriggeredResults());
    }
}