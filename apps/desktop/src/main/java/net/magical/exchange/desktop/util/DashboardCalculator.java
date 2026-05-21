package net.magical.exchange.desktop.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.magical.exchange.desktop.model.MarketRateDto;
import net.magical.exchange.desktop.model.RateOffer;
import net.magical.exchange.desktop.model.RateOfferKind;
import net.magical.exchange.desktop.model.UiLocale;

public final class DashboardCalculator {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private DashboardCalculator() {
	}

	public static List<MarketRateDto> ratesForCurrency(List<MarketRateDto> rates, String currencyCode) {
		return rates.stream().filter(rate -> rate.currency().code().equals(currencyCode)).toList();
	}

	public static List<RateOffer> buildRateOffers(List<MarketRateDto> rates, RateOfferKind kind) {
		List<RateOffer> offers = new ArrayList<>();

		for (MarketRateDto rate : rates) {
			BigDecimal value = kind == RateOfferKind.BUY ? rate.sellRate() : rate.buyRate();

			if (value != null) {
				offers.add(new RateOffer(rate, value));
			}
		}

		return offers.stream().sorted(compareRateOffers(kind)).toList();
	}

	public static BigDecimal parseAmount(String value) {
		String normalizedValue = value.trim().replace(',', '.');

		if (normalizedValue.isEmpty()) {
			return null;
		}

		try {
			return new BigDecimal(normalizedValue);
		} catch (NumberFormatException caughtError) {
			return null;
		}
	}

	public static BigDecimal convertBaseToForeign(BigDecimal amount, MarketRateDto rate) {
		if (amount == null || rate.sellRate() == null || BigDecimal.ZERO.compareTo(rate.sellRate()) >= 0) {
			return null;
		}

		return amount.multiply(BigDecimal.valueOf(rate.unit()), MATH_CONTEXT).divide(rate.sellRate(), MATH_CONTEXT);
	}

	public static BigDecimal convertForeignToBase(BigDecimal amount, MarketRateDto rate) {
		if (amount == null || rate.buyRate() == null || BigDecimal.ZERO.compareTo(rate.buyRate()) >= 0) {
			return null;
		}

		return amount.divide(BigDecimal.valueOf(rate.unit()), MATH_CONTEXT).multiply(rate.buyRate(), MATH_CONTEXT);
	}

	public static String formatBestRate(MarketRateDto rate, RateOfferKind kind, String baseCurrency, String foreignCurrency,
			UiLocale locale) {
		if (rate == null) {
			return "—";
		}

		BigDecimal value = kind == RateOfferKind.BUY ? rate.sellRate() : rate.buyRate();

		if (value == null) {
			return "—";
		}

		String formattedUnit = ExchangeFormat.formatNumber(rate.unit(), locale);
		String formattedValue = ExchangeFormat.formatRate(value, locale);

		return formattedUnit + " " + foreignCurrency + " = " + formattedValue + " " + baseCurrency;
	}

	private static Comparator<RateOffer> compareRateOffers(RateOfferKind kind) {
		return (left, right) -> {
			int rateOrder;

			if (kind == RateOfferKind.BUY) {
				rateOrder = left.value().compareTo(right.value());
			} else {
				rateOrder = right.value().compareTo(left.value());
			}

			if (rateOrder != 0) {
				return rateOrder;
			}

			int institutionOrder = left.rate().institution().name().compareTo(right.rate().institution().name());

			if (institutionOrder != 0) {
				return institutionOrder;
			}

			return left.rate().location().name().compareTo(right.rate().location().name());
		};
	}
}
