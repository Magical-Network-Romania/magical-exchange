import { formatCompactNumber, formatRate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { MarketRateDto } from "@/services/exchange-api";
import type { RateOffer, RateOfferKind } from "./types";

export function ratesForCurrency(rates: MarketRateDto[], currency: string) {
	return rates.filter((rate) => rate.currency.code === currency);
}

export function buildRateOffers(rates: MarketRateDto[], kind: RateOfferKind) {
	const offers: RateOffer[] = [];

	for (const rate of rates) {
		const value = kind === "buy" ? rate.sellRate : rate.buyRate;

		if (value !== null) {
			offers.push({ rate, value });
		}
	}

	return offers.sort((left, right) => compareRateOffers(left, right, kind));
}

export function parseAmount(value: string) {
	const normalizedValue = value.trim().replace(",", ".");

	if (!normalizedValue) {
		return null;
	}

	const parsedValue = Number(normalizedValue);

	return Number.isFinite(parsedValue) ? parsedValue : null;
}

export function convertBaseToForeign(amount: number | null, rate: MarketRateDto) {
	if (amount === null || rate.sellRate === null || rate.sellRate <= 0) {
		return null;
	}

	return (amount * rate.unit) / rate.sellRate;
}

export function convertForeignToBase(amount: number | null, rate: MarketRateDto) {
	if (amount === null || rate.buyRate === null || rate.buyRate <= 0) {
		return null;
	}

	return (amount / rate.unit) * rate.buyRate;
}

export function formatConvertedAmount(value: number | null, locale: UiLocale) {
	if (value === null || !Number.isFinite(value)) {
		return "—";
	}

	return formatRate(value, locale);
}

export function formatBestRate(
	rate: MarketRateDto | null,
	kind: RateOfferKind,
	baseCurrency: string,
	foreignCurrency: string,
	locale: UiLocale
) {
	if (!rate) {
		return "—";
	}

	const value = kind === "buy" ? rate.sellRate : rate.buyRate;

	if (value === null) {
		return "—";
	}

	return `${formatCompactNumber(rate.unit, locale)} ${foreignCurrency} = ${formatRate(value, locale)} ${baseCurrency}`;
}

function compareRateOffers(left: RateOffer, right: RateOffer, kind: RateOfferKind) {
	const rateOrder = kind === "buy" ? left.value - right.value : right.value - left.value;

	if (rateOrder !== 0) {
		return rateOrder;
	}

	return (
		left.rate.institution.name.localeCompare(right.rate.institution.name) ||
		left.rate.location.name.localeCompare(right.rate.location.name)
	);
}
