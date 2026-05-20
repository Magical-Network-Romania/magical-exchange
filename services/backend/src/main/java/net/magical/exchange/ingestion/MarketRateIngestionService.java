package net.magical.exchange.ingestion;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.magical.exchange.ingestion.exception.MarketRateIngestionException;
import net.magical.exchange.repository.CurrencyRepository;
import net.magical.exchange.repository.MarketRateRepository;
import net.magical.exchange.repository.MarketRateRepository.MarketRateUpsert;
import net.magical.exchange.repository.RateSourceRepository;
import net.magical.exchange.repository.RateSourceRepository.MarketRateSource;
import net.magical.exchange.repository.SourceFetchRunRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MarketRateIngestionService {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
	private static final Pattern RATE_ROW_PATTERN = Pattern
			.compile("(?:\\b|\\()([A-Z]{3})(?:\\b|\\))\\s+([-0-9.,]+)\\s+([-0-9.,]+)(?:\\s+([-0-9.,]+))?");
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

	private final CurrencyRepository currencyRepository;
	private final HttpClient httpClient;
	private final MarketRateRepository marketRateRepository;
	private final RateSourceRepository rateSourceRepository;
	private final SourceFetchRunRepository sourceFetchRunRepository;
	private final TransactionTemplate transactionTemplate;

	public MarketRateIngestionService(RateSourceRepository rateSourceRepository, SourceFetchRunRepository sourceFetchRunRepository,
			CurrencyRepository currencyRepository, MarketRateRepository marketRateRepository,
			PlatformTransactionManager transactionManager) {
		this.rateSourceRepository = rateSourceRepository;
		this.sourceFetchRunRepository = sourceFetchRunRepository;
		this.currencyRepository = currencyRepository;
		this.marketRateRepository = marketRateRepository;
		this.httpClient = buildHttpClient();
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public void fetchDueMarketRates() {
		List<MarketRateSource> sources = rateSourceRepository.findDueMarketSources();
		List<String> activeCurrencies = currencyRepository.findActiveCodes();

		for (MarketRateSource source : sources) {
			fetchSource(source, activeCurrencies);
		}
	}

	private HttpClient buildHttpClient() {
		HttpClient.Builder clientBuilder = HttpClient.newBuilder();

		clientBuilder.connectTimeout(CONNECT_TIMEOUT);
		clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);

		return clientBuilder.build();
	}

	private void fetchSource(MarketRateSource source, List<String> activeCurrencies) {
		UUID runId = sourceFetchRunRepository.start(source.id());

		try {
			String html = fetchHtml(source);
			List<MarketRateUpsert> rates = parseMarketRates(source, html, activeCurrencies);

			if (rates.isEmpty()) {
				sourceFetchRunRepository.finish(runId, "FAILED", 0, "No market rates found in source: " + source.slug());
				return;
			}

			String checksum = checksum(rates);
			saveFetchedRates(runId, source, checksum, rates);
		} catch (MarketRateIngestionException exception) {
			sourceFetchRunRepository.finish(runId, "FAILED", 0, exception.getMessage());
		} catch (DataAccessException exception) {
			sourceFetchRunRepository.finish(runId, "FAILED", 0, databaseFailureMessage(source.slug(), exception));
		}
	}

	int saveFetchedRates(UUID runId, MarketRateSource source, String checksum, List<MarketRateUpsert> rates) {
		Integer itemsInserted = transactionTemplate.execute(status -> {
			UUID batchId = marketRateRepository.insertBatch(source, checksum);

			if (batchId == null) {
				sourceFetchRunRepository.finish(runId, "SKIPPED", 0, "Market rates unchanged");
				rateSourceRepository.markFetched(source.id());
				return 0;
			}

			int inserted = marketRateRepository.insertRates(batchId, rates);

			sourceFetchRunRepository.finish(runId, "SUCCESS", inserted, null);
			rateSourceRepository.markFetched(source.id());

			return inserted;
		});

		if (itemsInserted == null) {
			return 0;
		}

		return itemsInserted;
	}

	private String databaseFailureMessage(String sourceSlug, DataAccessException exception) {
		return "Market rate ingestion database failure for " + sourceSlug + ": " + exception.getMessage();
	}

	private String fetchHtml(MarketRateSource source) {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(sourceUri(source));

		requestBuilder.timeout(REQUEST_TIMEOUT);
		requestBuilder.header("User-Agent", "MagicalExchangeBot/0.1 (+https://exchange.magical.md)");

		try {
			HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
			HttpResponse<String> response = httpClient.send(requestBuilder.GET().build(), bodyHandler);

			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				String message = "Market source returned HTTP " + response.statusCode() + ": " + source.slug();

				throw new MarketRateIngestionException(message);
			}

			return response.body();
		} catch (IOException exception) {
			throw new MarketRateIngestionException("Could not fetch market source: " + source.slug(), exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new MarketRateIngestionException("Market source fetch was interrupted: " + source.slug(), exception);
		}
	}

	private URI sourceUri(MarketRateSource source) {
		if (source.sourceUrl() == null || source.sourceUrl().isBlank()) {
			throw new MarketRateIngestionException("Market source URL is missing: " + source.slug());
		}

		try {
			return URI.create(source.sourceUrl());
		} catch (IllegalArgumentException exception) {
			throw new MarketRateIngestionException("Market source URL is invalid: " + source.slug(), exception);
		}
	}

	private List<MarketRateUpsert> parseMarketRates(MarketRateSource source, String html, List<String> activeCurrencies) {
		Document document = Jsoup.parse(html);
		String text = document.text();
		Matcher matcher = RATE_ROW_PATTERN.matcher(text);
		Map<String, MarketRateUpsert> ratesByCurrency = new LinkedHashMap<>();

		while (matcher.find()) {
			String currencyCode = matcher.group(1);

			if (!activeCurrencies.contains(currencyCode) || ratesByCurrency.containsKey(currencyCode)) {
				continue;
			}

			BigDecimal buyRate = parseOptionalDecimal(matcher.group(2));
			BigDecimal sellRate = parseOptionalDecimal(matcher.group(3));
			BigDecimal officialRate = parseOptionalDecimal(matcher.group(4));

			if (!hasVisibleRate(buyRate) && !hasVisibleRate(sellRate)) {
				continue;
			}

			MarketRateUpsert rate = new MarketRateUpsert(currencyCode, source.rateType(), buyRate, sellRate, officialRate, 1);

			ratesByCurrency.put(currencyCode, rate);
		}

		return new ArrayList<>(ratesByCurrency.values());
	}

	private BigDecimal parseOptionalDecimal(String value) {
		if (value == null || value.isBlank() || "-".equals(value.trim())) {
			return null;
		}

		try {
			return new BigDecimal(value.trim().replace(',', '.'));
		} catch (NumberFormatException exception) {
			throw new MarketRateIngestionException("Invalid market rate value: " + value, exception);
		}
	}

	private boolean hasVisibleRate(BigDecimal rate) {
		return rate != null && BigDecimal.ZERO.compareTo(rate) < 0;
	}

	private String checksum(List<MarketRateUpsert> rates) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			Comparator<MarketRateUpsert> currencyOrder = Comparator.comparing(MarketRateUpsert::currency);

			rates.stream().sorted(currencyOrder).forEach(rate -> updateDigest(digest, rate));

			return HexFormat.of().formatHex(digest.digest());
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	private void updateDigest(MessageDigest digest, MarketRateUpsert rate) {
		String value = String.join("|", rate.currency(), rate.type(), rateValue(rate.buy()), rateValue(rate.sell()),
				rateValue(rate.official()), Integer.toString(rate.unit()));

		digest.update(value.getBytes(StandardCharsets.UTF_8));
	}

	private String rateValue(BigDecimal rate) {
		if (rate == null) {
			return "";
		}

		return rate.stripTrailingZeros().toPlainString();
	}

}
