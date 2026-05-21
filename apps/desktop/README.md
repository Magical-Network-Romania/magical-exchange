# Magical Exchange Desktop

Native JavaFX desktop app for Magical Exchange.

## Run

Run the app with:

```bash
mvn -pl apps/desktop javafx:run
```

By default the app reads from the public production API at `https://exchange.magical.md/api/v1`.

For local development or staging, start the relevant backend and override the API URL without local storage or caching by setting either the environment variable:

```bash
MAGICAL_EXCHANGE_API_BASE_URL=http://localhost:8080/api/v1 mvn -pl apps/desktop javafx:run
```

or the JVM system property:

```bash
mvn -pl apps/desktop javafx:run -Dmagical.exchange.apiBaseUrl=http://localhost:8080/api/v1
```

The desktop client keeps only in-memory UI state. Countries, cities, current rates, market rates, and history are always fetched from the API.
