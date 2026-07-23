package com.synergisticit.stock_fetcher_service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="stock_data")
public class StockData {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String symbol;
	private BigDecimal price;
	
	@Column(name="changes") //"change is a keyword in MySQL, therefore it "change" can not be used as the name of the column.
	private BigDecimal change;
	
	private LocalDateTime fetchedAt;
	

}
