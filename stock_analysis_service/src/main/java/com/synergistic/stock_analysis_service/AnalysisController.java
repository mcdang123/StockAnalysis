package com.synergistic.stock_analysis_service;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {
	
	private final AnalysisService analysisService;
	
	public AnalysisController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}
	
	@GetMapping("/average/{symbol}")
	public BigDecimal getAverage(@PathVariable String symbol) {
		return analysisService.calculateAverage(symbol);
	}
	
	@GetMapping("/changePercent/{symbol}")
	public BigDecimal getDailyChange(@PathVariable String symbol) {
		return analysisService.calculateDailyChangePercent(symbol);
	}

}
