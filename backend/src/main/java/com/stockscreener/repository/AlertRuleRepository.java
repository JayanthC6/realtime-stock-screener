package com.stockscreener.repository;

import com.stockscreener.model.AlertRule;
import com.stockscreener.model.IndicatorType;
import com.stockscreener.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByUserAndActive(User user, Boolean active);
    List<AlertRule> findBySymbolAndActive(String symbol, Boolean active);
    List<AlertRule> findAllByActive(Boolean active);
    boolean existsByUserAndSymbolAndIndicatorType(User user, String symbol, IndicatorType indicatorType);
}