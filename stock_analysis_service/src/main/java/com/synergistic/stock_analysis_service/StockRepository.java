package com.synergistic.stock_analysis_service;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockData, Long> {
    List<StockData> findBySymbol(String symbol);
}
