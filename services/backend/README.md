# Magical Exchange Backend

Spring Boot API for Magical Exchange. The backend reads public data from PostgreSQL, exposes DB-backed endpoints for the
web/desktop clients, and runs scheduled ingestion for official national-bank and market exchange rates.

## Useful Commands

```sh
mvn verify
mvn spring-boot:run
mvn spotless:apply
```

## Endpoint

- `GET /api/v1/countries`
- `GET /api/v1/countries/{countryCode}/cities`
- `GET /api/v1/currencies`
- `GET /api/v1/official-rates?country=MD&date=YYYY-MM-DD`
- `GET /api/v1/official-rates/history?country=MD&currency=EUR&from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/v1/locations?country=MD&city=chisinau`
- `GET /api/v1/market-rates?country=MD&city=chisinau&currency=EUR&type=CASH`
- `GET /api/v1/market-rates/best?country=MD&city=chisinau&currency=EUR&operation=BUY_FOREIGN_CURRENCY`
- `GET /api/v1/bootstrap?country=MD&city=chisinau`
- `GET /actuator/health` returns Spring Boot health status.

## Environment

- `DATABASE_URL` - JDBC URL, defaults to `jdbc:postgresql://localhost:5432/magical_exchange`.
- `DATABASE_USERNAME` - defaults to `magical_exchange`.
- `DATABASE_PASSWORD` - defaults to `magical_exchange`.
- `MAGICAL_EXCHANGE_INGESTION_ENABLED` - defaults to `true`.
- `MAGICAL_EXCHANGE_INGESTION_FIXED_DELAY_MS` - defaults to `3600000`.
- `MAGICAL_EXCHANGE_INGESTION_STARTUP_ENABLED` - defaults to `true`.

Flyway remains enabled so the existing database migrations still run on startup.
