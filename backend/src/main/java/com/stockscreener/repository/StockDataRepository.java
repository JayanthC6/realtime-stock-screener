package com.stockscreener.repository;

import com.stockscreener.model.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockDataRepository extends JpaRepository<StockData, Long> {
    Optional<StockData> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);

    @Query("SELECT s FROM StockData s WHERE s.rsi > :minRsi AND s.rsi < :maxRsi")
    List<StockData> findByRsiBetween(Double minRsi, Double maxRsi);

    @Query("SELECT s FROM StockData s WHERE s.peRatio < :maxPe AND s.peRatio > 0")
    List<StockData> findByPeRatioLessThan(Double maxPe);

    @Query("SELECT s FROM StockData s WHERE s.volume > :minVolume")
    List<StockData> findByVolumeGreaterThan(Long minVolume);

    List<StockData> findAllByOrderBySymbolAsc();
}