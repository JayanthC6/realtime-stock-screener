package com.stockscreener.controller;

import com.stockscreener.dto.PriceHistoryDto;
import com.stockscreener.dto.ScreenerResultDto;
import com.stockscreener.dto.StockDataDto;
import com.stockscreener.screening.ScreeningEngine;
import com.stockscreener.service.StockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Live stock data, price history, and screener results")
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private final StockDataService stockDataService;
    private final ScreeningEngine screeningEngine;

    @GetMapping
    @Operation(summary = "Get all stocks (paginated)",
               description = "Returns paginated list of all tracked stocks sorted by symbol. " +
                             "Use ?page=0&size=20&sort=symbol for pagination.")
    @ApiResponse(responseCode = "200", description = "Paginated list of stocks")
    public ResponseEntity<Page<StockDataDto>> getAllStocks(
            @ParameterObject @PageableDefault(size = 20, sort = "symbol") Pageable pageable) {
        return ResponseEntity.ok(stockDataService.getAllStocks(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all stocks (non-paginated)",
               description = "Returns the full list of all tracked stocks without pagination.")
    public ResponseEntity<List<StockDataDto>> getAllStocksUnpaged() {
        return ResponseEntity.ok(stockDataService.getAllStocks());
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get stock by symbol")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock found"),
        @ApiResponse(responseCode = "404", description = "Stock not found")
    })
    public ResponseEntity<StockDataDto> getStockBySymbol(
            @PathVariable @Parameter(description = "Stock ticker e.g. AAPL") String symbol) {
        return ResponseEntity.ok(stockDataService.getStockBySymbol(symbol.toUpperCase()));
    }

    @GetMapping("/{symbol}/history")
    @Operation(summary = "Get price history for chart",
               description = "Returns timestamped price ticks for a symbol within the last N hours. " +
                             "Use ?hours=1 for 1-hour chart, ?hours=6 for 6-hour, etc.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Price history data"),
        @ApiResponse(responseCode = "404", description = "Symbol not found")
    })
    public ResponseEntity<List<PriceHistoryDto>> getStockHistory(
            @PathVariable @Parameter(description = "Stock ticker e.g. AAPL") String symbol,
            @RequestParam(defaultValue = "1") @Parameter(description = "Number of hours to look back") int hours) {
        return ResponseEntity.ok(stockDataService.getStockHistory(symbol.toUpperCase(), hours));
    }

    @GetMapping("/screener/results")
    @Operation(summary = "Get all screener evaluation results")
    public ResponseEntity<List<ScreenerResultDto>> getScreenerResults() {
        return ResponseEntity.ok(screeningEngine.getAllScreenerResults());
    }

    @GetMapping("/screener/triggered")
    @Operation(summary = "Get only stocks that have triggered screener conditions")
    public ResponseEntity<List<ScreenerResultDto>> getTriggeredResults() {
        return ResponseEntity.ok(screeningEngine.getTriggeredResults());
    }
}