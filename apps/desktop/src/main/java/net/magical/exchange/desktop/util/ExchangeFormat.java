package net.magical.exchange.desktop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import net.magical.exchange.desktop.model.UiLocale;

public final class ExchangeFormat {

	private ExchangeFormat() {
	}

	public static String formatDate(LocalDate value, UiLocale locale) {
		if (value == null) {
			return "—";
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale.toLocale());

		return formatter.format(value);
	}

	public static String formatDateTime(OffsetDateTime value, UiLocale locale) {
		if (value == null) {
			return "—";
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", locale.toLocale());

		return formatter.format(value);
	}

	public static String formatRate(BigDecimal value, UiLocale locale) {
		return formatRate(value, locale, 4);
	}

	public static String formatRate(BigDecimal value, UiLocale locale, int maximumFractionDigits) {
		if (value == null) {
			return "—";
		}

		NumberFormat formatter = NumberFormat.getNumberInstance(locale.toLocale());
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(maximumFractionDigits);

		return formatter.format(value);
	}

	public static String formatNumber(Number value, UiLocale locale) {
		NumberFormat formatter = NumberFormat.getNumberInstance(locale.toLocale());
		formatter.setMaximumFractionDigits(2);

		return formatter.format(value);
	}

	public static String formatConvertedAmount(BigDecimal value, UiLocale locale) {
		if (value == null) {
			return "—";
		}

		return formatRate(value, locale);
	}
}
