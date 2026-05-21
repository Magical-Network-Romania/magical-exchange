package net.magical.exchange.desktop.model;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum UiLocale {

	RO("ro", "Română", Locale.of("ro", "MD")), EN("en", "English", Locale.US), RU("ru", "Русский", Locale.of("ru", "RU"));

	private static final List<UiLocale> SUPPORTED = List.of(RO, EN, RU);

	private final String code;
	private final String label;
	private final Locale locale;

	UiLocale(String code, String label, Locale locale) {
		this.code = code;
		this.label = label;
		this.locale = locale;
	}

	public String code() {
		return code;
	}

	public String label() {
		return label;
	}

	public Locale toLocale() {
		return locale;
	}

	public static List<UiLocale> supported() {
		return SUPPORTED;
	}

	public static UiLocale detectDefault() {
		return fromLanguage(Locale.getDefault().getLanguage()).orElse(EN);
	}

	public static Optional<UiLocale> fromLanguage(String language) {
		String normalizedLanguage = language.toLowerCase(Locale.ROOT);

		return SUPPORTED.stream().filter(candidate -> candidate.code.equals(normalizedLanguage)).findFirst();
	}

	@Override
	public String toString() {
		return label;
	}
}
