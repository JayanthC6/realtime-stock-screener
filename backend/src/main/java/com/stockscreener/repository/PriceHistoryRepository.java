package com.stockscreener.repository;

import com.stockscreener.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // Last N prices for RSI calculation (most recent first)
    List<PriceHistory> findTop15BySymbolOrderByTimestampDesc(String symbol);

    // Date-range query for the history chart endpoint
    List<PriceHistory> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
            String symbol, LocalDateTime from, LocalDateTime to);

    // Count rows per symbol (useful for debugging / admin)
    long countBySymbol(String symbol);

    // Cleanup: delete history older than a given time (to prevent unbounded growth)
    @Query("DELETE FROM PriceHistory p WHERE p.timestamp < :cutoff")
    void deleteOlderThan(LocalDateTime cutoff);
}
