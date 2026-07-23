package com.synergistic.stock_api_gateway.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/stock")
public class StockFlowController {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fetcher.url}")
    private String fetcherUrl;

    @Value("${analysis.url}")
    private String analysisUrl;

    @Value("${pattern.url}")
    private String patternUrl;

    @RequestMapping("full-analysis/{symbol}")
    public ResponseEntity<Map<String, Object>> fullStockAnalysis(@PathVariable String symbol) {
        symbol = symbol.trim().toUpperCase();
        Map<String, Object> result = new HashMap<>();

        try {
            //===========Fetcher Service================================
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> stockData = (Map<String, Object>) restTemplate.postForObject(fetcherUrl + "/stocks/fetch/" + symbol, null, Map.class);
                result.put("stockData", stockData);
            } catch (Exception e) {
                result.put("stockDataError", "Fetcher Service failed: " + e.getMessage());
            }

            //====================Analysis Service====================
            try {
                BigDecimal avg = restTemplate.getForObject(analysisUrl + "/analysis/average/" + symbol, BigDecimal.class);
                BigDecimal change = restTemplate.getForObject(analysisUrl + "/analysis/changePercent/" + symbol, BigDecimal.class);
                result.put("averagePrice", avg);
                result.put("dailyChangePercent", change);
            } catch (Exception e) {
                result.put("analysisError", "Analysis Service failed: " + e.getMessage());
            }

            //====================Pattern Service====================
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object>[] pattern = (Map<String, Object>[]) restTemplate.postForObject(patternUrl + "/patterns/analyze/" + symbol + "/1D", null, Map[].class);
                result.put("pattern", pattern);
            } catch (Exception e) {
                result.put("patternError", "Pattern Service failed: " + e.getMessage());
            }

            result.put("status", "SUCCESS");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

}
