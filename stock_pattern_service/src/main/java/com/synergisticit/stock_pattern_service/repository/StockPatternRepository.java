package com.synergisticit.stock_pattern_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synergisticit.stock_pattern_service.model.StockPattern;

public interface StockPatternRepository extends JpaRepository<StockPattern, Long> {

	List<StockPattern> findBySymbol(String symbol);
}
