package com.synergisticit.stock_fetcher_service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StockService {
	private final StockRepository stockRepository;
	private final RestTemplate restTemplate = new RestTemplate();
	private final ConcurrentHashMap<String, StockData> cache = new ConcurrentHashMap<>();

	@Value("${finnhub.api.key}")
	private String apiKey;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	public StockData fetchStock(String symbol) {
		try {
			if (cache.containsKey(symbol)) {
				StockData cached = cache.get(symbol);
				if (cached.getFetchedAt().isAfter(LocalDateTime.now().minusMinutes(10))) {
					return cached;
				}
			}
			String url = String.format("https://finnhub.io/api/v1/quote?symbol=%s&token=%s", symbol.toUpperCase(), apiKey);
			Map<String, Object> response = restTemplate.getForObject(url, Map.class);

			if (response == null || response.get("c") == null) {
				System.err.println("Empty response from Finnhub for symbol: " + symbol);
				return null;
			}

			StockData data = new StockData();
			data.setSymbol(symbol.toUpperCase());
			data.setPrice(new BigDecimal(response.get("c").toString()));
			data.setChange(new BigDecimal(response.get("d").toString()));
			data.setFetchedAt(LocalDateTime.now());

			cache.put(symbol, data);
			stockRepository.save(data);
			return data;
		} catch (Exception e) {
			System.err.println("Error fetching stock " + symbol + ": " + e.getMessage());
		}
		return null;
	}

}
