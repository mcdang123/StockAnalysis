package com.synergisticit.stock_fetcher_service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
public class StockController {
	public final StockService stockService;
	
	public StockController(StockService stockService) {
		this.stockService = stockService;
	}
	
	@PostMapping("/fetch/{symbol}")
	public StockData fetchStock(@PathVariable String symbol){
		return stockService.fetchStock(symbol);
	}
	

}
