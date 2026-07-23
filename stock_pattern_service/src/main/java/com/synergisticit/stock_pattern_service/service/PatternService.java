package com.synergisticit.stock_pattern_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.synergisticit.stock_pattern_service.model.StockPattern;
import com.synergisticit.stock_pattern_service.repository.StockPatternRepository;



@Service
public class PatternService {
	private final StockPatternRepository stockPatternRepository;
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${analysis.service.url}")
	private String analysisUrl;
	
	public PatternService(StockPatternRepository repository ){
		this.stockPatternRepository = repository;
	}

	public List<StockPattern> getPatterns(String symbol){
		try {
			return stockPatternRepository.findBySymbol(symbol);
		}catch(Exception e) {
			System.err.println("Could not fetch patterns for "+symbol+": "+e.getMessage());
			return Collections.emptyList();
		}
	}
	
	public List<StockPattern> analyzeStock(String symbol, String timeFrame){
		symbol = symbol.trim().toUpperCase();
		BigDecimal avg = BigDecimal.ZERO;
		BigDecimal change = BigDecimal.ZERO;
		
		//1. fetching average price form Analysis Service
		try {
			avg = restTemplate.getForObject(analysisUrl+"average/"+symbol, BigDecimal.class);
		}catch(HttpServerErrorException e) {
			System.err.println("Analysis service returned 500 for avg(" +symbol+"): "+e.getMessage());
		}catch(RestClientException e) {
			System.err.println("Could not reach Analysis Service for avg(\" +symbol+\")\": "+e.getMessage());
		}
		
		//1. fetching daily percent form Analysis Service
		try {
			change = restTemplate.getForObject(analysisUrl+"changePercent/"+symbol, BigDecimal.class);
		}catch(HttpServerErrorException e) {
			System.err.println("Analysis service returned 500 for change(" +symbol+"): "+e.getMessage());
		}catch(RestClientException e) {
			System.err.println("Could not reach Analysis Service for chnage(\" +symbol+\")\": "+e.getMessage());
		}
		
		//default values for change and average
		if(avg==null) avg= BigDecimal.ZERO;
		if(change==null) change = BigDecimal.ZERO;
		
		//detecting simple patterns
		String patternType;
		if(change.compareTo(BigDecimal.valueOf(2)) >0) {
			patternType="Uptrend";
		}else if(change.compareTo(BigDecimal.valueOf(2)) <0) {
			patternType="Downtrend";
		}else {
			patternType="Sideways";
		}
		
		//save the detected pattern
		try {
			StockPattern pattern = new StockPattern();
			pattern.setSymbol(symbol);
			pattern.setPatternType(patternType);
			pattern.setTimeFrame(timeFrame);
			pattern.setDetectedAt(LocalDateTime.now());
			stockPatternRepository.save(pattern);
			return List.of(pattern);
		}catch(Exception e) {
			System.err.println("Failed to save pattern for "+symbol +": "+e.getMessage());
			return Collections.emptyList();
		}
		
	}
	
	@Scheduled(fixedRate = 3600000) // every 1 hour
	public void autoPatternDetection() {
		try {
			System.out.println("Scheduled pattern detection starts:");
			analyzeStock("WMT", "1D");
			analyzeStock("CSCO", "1D");
			analyzeStock("IBM", "1D");
			analyzeStock("EBAY", "1D");
			analyzeStock("NDAQ", "1D");
			analyzeStock("LOGI", "1D");
			analyzeStock("COKE", "1D");
			analyzeStock("PEP", "1D");
			
		}catch(Exception e) {
			System.err.println("Scheduled pattern detection failed: "+e.getMessage());
		}
	}
}
