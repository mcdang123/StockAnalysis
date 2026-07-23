package com.synergisticit.stock_fetcher_service;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockData, Long> {

}
