package net.magical.exchange.desktop.model;

public record AppState(UiLocale locale, String countryCode, String citySlug, String selectedCurrency, BootstrapDto bootstrap,
		boolean bootstrapLoading, String bootstrapError) {
}
