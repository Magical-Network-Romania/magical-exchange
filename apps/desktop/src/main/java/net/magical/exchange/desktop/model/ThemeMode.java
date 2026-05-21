package net.magical.exchange.desktop.model;

public enum ThemeMode {

	LIGHT("light"), DARK("dark");

	private final String cssClass;

	ThemeMode(String cssClass) {
		this.cssClass = cssClass;
	}

	public String cssClass() {
		return cssClass;
	}

	public ThemeMode next() {
		return this == DARK ? LIGHT : DARK;
	}
}
