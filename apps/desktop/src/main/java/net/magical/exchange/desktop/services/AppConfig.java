package net.magical.exchange.desktop.services;

import java.net.URI;

public final class AppConfig {

	// Public production default for packaged desktop builds; override locally with
	// the property or env var below.
	private static final String DEFAULT_API_BASE_URL = "https://exchange.magical.md/api/v1";
	private static final String API_BASE_URL_PROPERTY = "magical.exchange.apiBaseUrl";
	private static final String API_BASE_URL_ENV = "MAGICAL_EXCHANGE_API_BASE_URL";

	private AppConfig() {
	}

	public static URI apiBaseUri() {
		String propertyValue = System.getProperty(API_BASE_URL_PROPERTY);

		if (propertyValue != null && !propertyValue.isBlank()) {
			return URI.create(stripTrailingSlash(propertyValue));
		}

		String environmentValue = System.getenv(API_BASE_URL_ENV);

		if (environmentValue != null && !environmentValue.isBlank()) {
			return URI.create(stripTrailingSlash(environmentValue));
		}

		return URI.create(DEFAULT_API_BASE_URL);
	}

	private static String stripTrailingSlash(String value) {
		String trimmedValue = value.trim();

		while (trimmedValue.endsWith("/")) {
			trimmedValue = trimmedValue.substring(0, trimmedValue.length() - 1);
		}

		return trimmedValue;
	}
}
