import { toIntlLocale, type UiLocale } from "@/i18n";

export function formatDate(value: string | null, locale: UiLocale) {
	if (!value) {
		return "—";
	}

	return new Intl.DateTimeFormat(toIntlLocale(locale), {
		day: "2-digit",
		month: "short",
		year: "numeric"
	}).format(new Date(`${value}T00:00:00`));
}

export function formatDateTime(value: string | null, locale: UiLocale) {
	if (!value) {
		return "—";
	}

	return new Intl.DateTimeFormat(toIntlLocale(locale), {
		day: "2-digit",
		hour: "2-digit",
		minute: "2-digit",
		month: "short"
	}).format(new Date(value));
}

export function formatRate(value: number | null, locale: UiLocale, maximumFractionDigits = 4) {
	if (value === null || Number.isNaN(value)) {
		return "—";
	}

	return new Intl.NumberFormat(toIntlLocale(locale), {
		maximumFractionDigits,
		minimumFractionDigits: 2
	}).format(value);
}

export function formatCompactNumber(value: number, locale: UiLocale) {
	return new Intl.NumberFormat(toIntlLocale(locale), {
		maximumFractionDigits: 2
	}).format(value);
}

export function todayIsoDate() {
	return new Date().toISOString().slice(0, 10);
}

export function daysAgoIsoDate(days: number) {
	const date = new Date();
	date.setDate(date.getDate() - days);

	return date.toISOString().slice(0, 10);
}
