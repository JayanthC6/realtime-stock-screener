package com.stockscreener.repository;

import com.stockscreener.model.AlertHistory;
import com.stockscreener.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    List<AlertHistory> findByUserOrderByTriggeredAtDesc(User user);
    List<AlertHistory> findBySymbolOrderByTriggeredAtDesc(String symbol);
}