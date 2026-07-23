package com.synergisticit.stock_fetcher_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockFetcherServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockFetcherServiceApplication.class, args);
	}

}
