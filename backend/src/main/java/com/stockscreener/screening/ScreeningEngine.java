package com.stockscreener.screening;

import com.stockscreener.dto.ScreenerResultDto;
import com.stockscreener.dto.StockDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScreeningEngine {

    private final SimpMessagingTemplate messagingTemplate;

    // In-memory store of latest screener results
    private final Map<String, ScreenerResultDto> screenerResults = new ConcurrentHashMap<>();

    // Configurable thresholds
    private static final double PE_RATIO_THRESHOLD = 25.0;
    private static final double RSI_OVERBOUGHT = 70.0;
    private static final double RSI_OVERSOLD = 30.0;
    private static final long VOLUME_SPIKE_THRESHOLD = 1_000_000L;

    public void screen(StockDataDto stockDataDto) {
        ScreenerResultDto result = evaluate(stockDataDto);
        screenerResults.put(stockDataDto.getSymbol(), result);

        // Push screener result to frontend if any indicator is triggered
        if (result.getPeAlert() || result.getRsiOverbought()
                || result.getRsiOversold() || result.getVolumeSpike()) {
            messagingTemplate.convertAndSend("/topic/screener", result);
            log.debug("Screener alert triggered for: {}", stockDataDto.getSymbol());
        }
    }

    private ScreenerResultDto evaluate(StockDataDto dto) {
        boolean peAlert = dto.getPeRatio() != null
                && dto.getPeRatio() > PE_RATIO_THRESHOLD;

        boolean rsiOverbought = dto.getRsi() != null
                && dto.getRsi() > RSI_OVERBOUGHT;

        boolean rsiOversold = dto.getRsi() != null
                && dto.getRsi() < RSI_OVERSOLD;

        boolean volumeSpike = dto.getVolume() != null
                && dto.getVolume() > VOLUME_SPIKE_THRESHOLD;

        return ScreenerResultDto.builder()
                .symbol(dto.getSymbol())
                .currentPrice(dto.getCurrentPrice())
                .peRatio(dto.getPeRatio())
                .rsi(dto.getRsi())
                .volume(dto.getVolume())
                .peAlert(peAlert)
                .rsiOverbought(rsiOverbought)
                .rsiOversold(rsiOversold)
                .volumeSpike(volumeSpike)
                .build();
    }

    public List<ScreenerResultDto> getAllScreenerResults() {
        return new ArrayList<>(screenerResults.values());
    }

    public List<ScreenerResultDto> getTriggeredResults() {
        return screenerResults.values().stream()
                .filter(r -> r.getPeAlert() || r.getRsiOverbought()
                        || r.getRsiOversold() || r.getVolumeSpike())
                .toList();
    }
}