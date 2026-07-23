package com.synergisticit.stock_pattern_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergisticit.stock_pattern_service.model.StockPattern;
import com.synergisticit.stock_pattern_service.service.PatternService;

@RestController
@RequestMapping("/patterns")
public class PatternController {
	
	private final PatternService patternService;
	
	public PatternController(PatternService patternService){
		this.patternService = patternService;
	}
	
	
	@GetMapping("/{symbol}")
	public List<StockPattern> getPatterns(@PathVariable String symbol){
		return patternService.getPatterns(symbol);
	}
	
	@PostMapping("/analyze/{symbol}/{timeFrame}")
	public List<StockPattern> analyzeStock(@PathVariable String symbol, @PathVariable String timeFrame){
		return patternService.analyzeStock(symbol, timeFrame);
	}
	

}
