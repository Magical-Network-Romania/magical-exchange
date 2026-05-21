package net.magical.exchange.desktop.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.magical.exchange.desktop.model.BootstrapDto;
import net.magical.exchange.desktop.model.CityDto;
import net.magical.exchange.desktop.model.CountryDto;
import net.magical.exchange.desktop.model.OfficialRateHistoryPoint;
import net.magical.exchange.desktop.model.UiLocale;

public class ExchangeApiClient {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

	private final URI apiBaseUri;
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public ExchangeApiClient(URI apiBaseUri) {
		this.apiBaseUri = apiBaseUri;
		httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public CompletableFuture<List<CountryDto>> fetchCountries(UiLocale locale) {
		return getArray("/countries", Map.of("locale", locale.code()), CountryDto[].class);
	}

	public CompletableFuture<List<CityDto>> fetchCities(String countryCode, UiLocale locale) {
		return getArray("/countries/" + encodePath(countryCode) + "/cities", Map.of("locale", locale.code()), CityDto[].class);
	}

	public CompletableFuture<BootstrapDto> fetchBootstrap(String countryCode, String citySlug, UiLocale locale) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("country", countryCode);
		params.put("city", citySlug);
		params.put("locale", locale.code());

		return get("/bootstrap", params, BootstrapDto.class);
	}

	public CompletableFuture<List<OfficialRateHistoryPoint>> fetchOfficialRateHistory(String countryCode, String currencyCode,
			LocalDate from, LocalDate to) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("country", countryCode);
		params.put("currency", currencyCode);
		params.put("from", from.toString());
		params.put("to", to.toString());

		return getArray("/official-rates/history", params, OfficialRateHistoryPoint[].class);
	}

	private <T> CompletableFuture<List<T>> getArray(String path, Map<String, String> params, Class<T[]> responseType) {
		return get(path, params, responseType).thenApply(response -> List.copyOf(Arrays.asList(response)));
	}

	private <T> CompletableFuture<T> get(String path, Map<String, String> params, Class<T> responseType) {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(buildUri(path, params));
		HttpRequest request = requestBuilder.timeout(REQUEST_TIMEOUT).header("Accept", "application/json").GET().build();

		return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
				.thenApply(response -> parseResponse(response, responseType));
	}

	private URI buildUri(String path, Map<String, String> params) {
		StringBuilder builder = new StringBuilder(apiBaseUri.toString()).append(path);

		if (!params.isEmpty()) {
			builder.append('?');
			boolean first = true;

			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (!first) {
					builder.append('&');
				}

				builder.append(encodeQuery(entry.getKey())).append('=').append(encodeQuery(entry.getValue()));
				first = false;
			}
		}

		return URI.create(builder.toString());
	}

	private <T> T parseResponse(HttpResponse<String> response, Class<T> responseType) {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			String message = response.body().isBlank() ? "API returned " + response.statusCode() : response.body();
			throw new ApiClientException(message);
		}

		try {
			return objectMapper.readValue(response.body(), responseType);
		} catch (JsonProcessingException caughtError) {
			throw new ApiClientException("Invalid API response", caughtError);
		}
	}

	private static String encodePath(String value) {
		return encodeQuery(value).replace("+", "%20");
	}

	private static String encodeQuery(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException caughtError) {
			throw new ApiClientException("UTF-8 is not available", caughtError);
		}
	}
}
