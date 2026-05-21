package net.magical.exchange.desktop.model;

public enum PageRoute {

	DASHBOARD("currentRates"), RATES("allRates"), HISTORY("history");

	private final String translationKey;

	PageRoute(String translationKey) {
		this.translationKey = translationKey;
	}

	public String translationKey() {
		return translationKey;
	}
}
