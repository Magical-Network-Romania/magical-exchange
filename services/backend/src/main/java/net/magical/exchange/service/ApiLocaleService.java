package net.magical.exchange.service;

import java.util.Locale;
import java.util.regex.Pattern;
import net.magical.exchange.repository.LocalizationRepository;
import org.springframework.stereotype.Service;

@Service
public class ApiLocaleService {

	private static final String DEFAULT_LOCALE = "ro";
	private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2}(-[A-Z]{2})?$");

	private final LocalizationRepository localizationRepository;

	public ApiLocaleService(LocalizationRepository localizationRepository) {
		this.localizationRepository = localizationRepository;
	}

	public String resolve(String locale, String acceptLanguage) {
		String candidate = normalize(firstPresent(locale, acceptLanguage));

		if (isSupported(candidate)) {
			return candidate;
		}

		String languageOnly = languageOnly(candidate);

		if (isSupported(languageOnly)) {
			return languageOnly;
		}

		return DEFAULT_LOCALE;
	}

	private String firstPresent(String locale, String acceptLanguage) {
		if (locale != null && !locale.isBlank()) {
			return locale;
		}

		if (acceptLanguage == null || acceptLanguage.isBlank()) {
			return DEFAULT_LOCALE;
		}

		return acceptLanguage.split(",", 2)[0].split(";", 2)[0];
	}

	private String normalize(String locale) {
		String[] parts = locale.trim().replace('_', '-').split("-", 2);
		String language = parts[0].toLowerCase(Locale.ROOT);

		if (parts.length == 1) {
			return language;
		}

		return language + "-" + parts[1].toUpperCase(Locale.ROOT);
	}

	private String languageOnly(String locale) {
		if (locale == null) {
			return DEFAULT_LOCALE;
		}

		return locale.split("-", 2)[0];
	}

	private boolean isSupported(String locale) {
		if (locale == null || !LOCALE_PATTERN.matcher(locale).matches()) {
			return false;
		}

		return localizationRepository.existsActive(locale);
	}
}
