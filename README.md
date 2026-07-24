# Stock Analysis

A microservices-based stock analysis platform: fetches quotes, runs analysis, and detects patterns, all behind a single API gateway.

## Architecture

```
Client
  |
  v
stock_api_gateway (8080)
  |-- /stocks/**    -> stock_fetcher_service (8081)
  |-- /analysis/**  -> stock_analysis_service (8082)
  |-- /patterns/**  -> stock_pattern_service (8083)
```

- **stock_fetcher_service** pulls live quotes from the [Finnhub](https://finnhub.io/) API and persists them.
- **stock_analysis_service** calls the fetcher service and computes analysis on the data.
- **stock_pattern_service** calls the analysis service and detects chart patterns.
- **stock_api_gateway** is the single entry point that routes to the three services above.

All 3 backend services share one MySQL server (separate databases: `fetcher_db`, `analysis_db`, `pattern_db`, auto-created on first connection).

## Prerequisites

- Docker + Docker Compose, **or**
- JDK 17, Maven (or the bundled `./mvnw` wrapper per module), and a local MySQL 8 instance
- A free [Finnhub API key](https://finnhub.io/register)

## Run with Docker Compose (recommended)

```bash
cp .env.example .env
# edit .env and set FINNHUB_API_KEY (and DB_PASSWORD if you want a non-default one)
docker-compose up --build
```

This starts MySQL and all 4 services. Once healthy, open `http://localhost:8080` for the web UI (search a ticker symbol to see price/change/average/pattern data), or hit the gateway's JSON API directly at `http://localhost:8080/stock/full-analysis/{symbol}`.

## Run locally without Docker

Start each service in a separate terminal, in this order (each depends on the one before it being reachable):

```bash
cd stock_fetcher_service   && ./mvnw spring-boot:run   # 8081
cd stock_analysis_service  && ./mvnw spring-boot:run   # 8082
cd stock_pattern_service   && ./mvnw spring-boot:run   # 8083
cd stock_api_gateway       && ./mvnw spring-boot:run   # 8080
```

Set `DB_PASSWORD` and `FINNHUB_API_KEY` as environment variables first, or edit each module's `application.properties` locally (don't commit real values).

## API testing

A Postman collection is included: `StockAnalysis.postman_collection.json`.
