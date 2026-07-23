package com.synergistic.stock_analysis_service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AnalysisService {
	private final RestTemplate restTemplate = new RestTemplate();
	private final StockRepository stockRepository;

	@Value("${fetcher.service.url}")
	private String fetcherUrl;

	public AnalysisService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	public BigDecimal calculateAverage(String symbol) {
		symbol = symbol.trim().toUpperCase();
		try {
			StockData fetched = restTemplate.postForObject(fetcherUrl + symbol, null, StockData.class);
			if (fetched == null || fetched.getPrice() == null) {
				System.out.println("Null response for " + symbol + " from fetcher.");
				return BigDecimal.ZERO;
			}

			// Persist fetched data so history accumulates in analysis_db
			StockData toSave = new StockData();
			toSave.setSymbol(fetched.getSymbol());
			toSave.setPrice(fetched.getPrice());
			toSave.setChange(fetched.getChange());
			toSave.setFetchedAt(LocalDateTime.now());
			stockRepository.save(toSave);

			// Compute true average across all saved records for this symbol
			List<StockData> records = stockRepository.findBySymbol(symbol);
			if (records.isEmpty()) return BigDecimal.ZERO;
			BigDecimal sum = records.stream()
					.map(StockData::getPrice)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			return sum.divide(BigDecimal.valueOf(records.size()), 4, RoundingMode.HALF_UP);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			System.out.println("Fetcher API returned error for symbol " + symbol + ": " + e.getMessage());
			return BigDecimal.ZERO;
		} catch (RestClientException e) {
			System.out.println("Network problem while accessing fetcher for " + symbol + ": " + e.getMessage());
			return BigDecimal.ZERO;
		} catch (Exception e) {
			System.out.println("Unexpected error calculating average for: " + symbol);
			return BigDecimal.ZERO;
		}
	}

	public BigDecimal calculateDailyChangePercent(String symbol) {
		symbol = symbol.trim().toUpperCase();
		try {
			StockData data = restTemplate.postForObject(fetcherUrl + symbol, null, StockData.class);
			if (data == null || data.getPrice() == null || data.getChange() == null) {
				System.out.println("Incomplete data for " + symbol);
				return BigDecimal.ZERO;
			}
			if (data.getPrice().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
			return data.getChange().divide(data.getPrice(), 4, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100));

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			System.out.println("Fetcher API returned error for symbol " + symbol + ": " + e.getMessage());
			return BigDecimal.ZERO;
		} catch (RestClientException e) {
			System.out.println("Network problem while accessing fetcher for " + symbol + ": " + e.getMessage());
			return BigDecimal.ZERO;
		} catch (Exception e) {
			System.out.println("Unexpected error calculating daily change percent for: " + symbol);
			return BigDecimal.ZERO;
		}
	}
}
