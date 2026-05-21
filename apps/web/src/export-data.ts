import type { BootstrapDto, MarketRateDto, OfficialRateHistoryPoint } from "@/services/exchange-api";

export type ExportFormat = "csv" | "txt";

const csvSpecialCharactersPattern = /[",\n\r]/;
const safeFileNamePattern = /[^a-z0-9-]+/g;
const fileNameTrimPattern = /(^-+|-+$)/g;

type MarketRatesExportOptions = {
	bootstrap: BootstrapDto;
	format: ExportFormat;
	rates: MarketRateDto[];
	selectedCurrency: string | null;
};

type HistoryExportOptions = {
	baseCurrency: string;
	countryCode: string;
	countryName: string;
	currency: string;
	format: ExportFormat;
	from: string;
	history: OfficialRateHistoryPoint[];
	to: string;
};

export function exportMarketRates({ bootstrap, format, rates, selectedCurrency }: MarketRatesExportOptions) {
	const currencyLabel = selectedCurrency ?? "all";
	const content = format === "csv" ? marketRatesCsv(bootstrap, rates, currencyLabel) : marketRatesTxt(bootstrap, rates, currencyLabel);
	downloadExport(`current-rates-${bootstrap.country.code}-${bootstrap.city.slug}-${currencyLabel}`, format, content);
}

export function exportOfficialRateHistory({
	baseCurrency,
	countryCode,
	countryName,
	currency,
	format,
	from,
	history,
	to
}: HistoryExportOptions) {
	const content =
		format === "csv"
			? historyCsv(countryCode, countryName, baseCurrency, currency, from, to, history)
			: historyTxt(countryCode, countryName, baseCurrency, currency, from, to, history);
	downloadExport(`official-history-${countryCode}-${currency}-${from}-${to}`, format, content);
}

function marketRatesCsv(bootstrap: BootstrapDto, rates: MarketRateDto[], currencyLabel: string) {
	const rows = rates.map((rate) => [
		bootstrap.country.code,
		bootstrap.country.name,
		bootstrap.city.slug,
		bootstrap.city.name,
		currencyLabel,
		rate.institution.name,
		rate.location.name,
		rate.location.address,
		rate.currency.code,
		rate.currency.name,
		rate.rateType,
		rate.unit,
		rate.buyRate,
		rate.sellRate,
		rate.officialRate,
		rate.publishedAt,
		rate.fetchedAt
	]);

	return toCsv([
		[
			"country_code",
			"country",
			"city_slug",
			"city",
			"selection",
			"institution",
			"office",
			"address",
			"currency",
			"currency_name",
			"rate_type",
			"unit",
			"buy_rate",
			"sell_rate",
			"official_rate",
			"published_at",
			"fetched_at"
		],
		...rows
	]);
}

function marketRatesTxt(bootstrap: BootstrapDto, rates: MarketRateDto[], currencyLabel: string) {
	return [
		"Current bank exchange rates",
		`Country: ${bootstrap.country.name} (${bootstrap.country.code})`,
		`City: ${bootstrap.city.name}`,
		`Currency selection: ${currencyLabel}`,
		`Generated at: ${bootstrap.generatedAt}`,
		"",
		...rates.map((rate) =>
			[
				`${rate.institution.name} - ${rate.location.name}`,
				`Address: ${rate.location.address}`,
				`Currency: ${rate.currency.code} (${rate.currency.name})`,
				`Rate type: ${rate.rateType}`,
				`Unit: ${rate.unit}`,
				`Buy: ${formatExportValue(rate.buyRate)}`,
				`Sell: ${formatExportValue(rate.sellRate)}`,
				`Official rate: ${formatExportValue(rate.officialRate)}`,
				`Published at: ${formatExportValue(rate.publishedAt)}`,
				`Fetched at: ${rate.fetchedAt}`
			].join("\n")
		)
	].join("\n\n");
}

function historyCsv(
	countryCode: string,
	countryName: string,
	baseCurrency: string,
	currency: string,
	from: string,
	to: string,
	history: OfficialRateHistoryPoint[]
) {
	const rows = history.map((point) => [
		countryCode,
		countryName,
		baseCurrency,
		currency,
		from,
		to,
		point.rateDate,
		point.unit,
		point.rate,
		point.fetchedAt
	]);

	return toCsv([
		["country_code", "country", "base_currency", "currency", "from", "to", "rate_date", "unit", "rate", "fetched_at"],
		...rows
	]);
}

function historyTxt(
	countryCode: string,
	countryName: string,
	baseCurrency: string,
	currency: string,
	from: string,
	to: string,
	history: OfficialRateHistoryPoint[]
) {
	return [
		"Official national-bank rate history",
		`Country: ${countryName} (${countryCode})`,
		`Currency: ${currency} / ${baseCurrency}`,
		`Range: ${from} - ${to}`,
		"",
		...history.map((point) =>
			[`Date: ${point.rateDate}`, `Unit: ${point.unit}`, `Rate: ${point.rate}`, `Fetched at: ${point.fetchedAt}`].join("\n")
		)
	].join("\n\n");
}

function toCsv(rows: unknown[][]) {
	return rows.map((row) => row.map(csvCell).join(",")).join("\n");
}

function csvCell(value: unknown) {
	const text = formatExportValue(value);

	if (csvSpecialCharactersPattern.test(text)) {
		return `"${text.replaceAll('"', '""')}"`;
	}

	return text;
}

function formatExportValue(value: unknown) {
	return value === null || value === undefined ? "" : String(value);
}

function downloadExport(fileBaseName: string, format: ExportFormat, content: string) {
	const blob = new Blob([content], {
		type: format === "csv" ? "text/csv;charset=utf-8" : "text/plain;charset=utf-8"
	});
	const link = document.createElement("a");
	const url = URL.createObjectURL(blob);

	link.href = url;
	link.download = `${safeFileName(fileBaseName)}.${format}`;
	document.body.append(link);
	link.click();
	link.remove();
	URL.revokeObjectURL(url);
}

function safeFileName(value: string) {
	return value.toLowerCase().replaceAll(safeFileNamePattern, "-").replaceAll(fileNameTrimPattern, "");
}
