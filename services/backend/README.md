# Magical Exchange Backend

Minimal Spring Boot API used to smoke-test deployment.

## Useful Commands

```sh
mvn verify
mvn spring-boot:run
mvn spotless:apply
```

## Endpoint

- `GET /api/hello` returns a small JSON response.
- `GET /actuator/health` returns Spring Boot health status.

## Environment

- `DATABASE_URL` - JDBC URL, defaults to `jdbc:postgresql://localhost:5432/magical_exchange`.
- `DATABASE_USERNAME` - defaults to `magical_exchange`.
- `DATABASE_PASSWORD` - defaults to `magical_exchange`.

Flyway remains enabled so the existing database migrations still run on startup.
