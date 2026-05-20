export type CurrencyDto = {
	code: string;
	name: string;
	numericCode: string | null;
	minorUnits: number;
};

export type CountryDto = {
	code: string;
	name: string;
	baseCurrencyCode: string;
	defaultLocaleCode: string;
};

export type CityDto = {
	countryCode: string;
	slug: string;
	name: string;
	timezone: string;
};

export type InstitutionDto = {
	id: string;
	slug: string;
	name: string;
	type: string;
	websiteUrl: string | null;
};

export type LocationDto = {
	id: string;
	countryCode: string;
	citySlug: string;
	slug: string;
	name: string;
	address: string;
	lat: number | null;
	lng: number | null;
	phone: string | null;
	email: string | null;
	institution: InstitutionDto;
};

export type OfficialRateDto = {
	currency: CurrencyDto;
	unit: number;
	rate: number;
	source: string | null;
	fetchedAt: string;
};

export type OfficialRatesDto = {
	country: string;
	baseCurrency: string;
	rateDate: string | null;
	rates: OfficialRateDto[];
};

export type MarketRateDto = {
	currency: CurrencyDto;
	rateType: string;
	buyRate: number | null;
	sellRate: number | null;
	officialRate: number | null;
	unit: number;
	fetchedAt: string;
	publishedAt: string | null;
	institution: InstitutionDto;
	location: LocationDto;
};

export type BestMarketRatesDto = {
	countryCode: string;
	citySlug: string;
	currencyCode: string;
	rateType: string;
	operation: BestRateOperation;
	rates: MarketRateDto[];
};

export type BootstrapDto = {
	generatedAt: string;
	country: CountryDto;
	city: CityDto;
	currencies: CurrencyDto[];
	officialRates: OfficialRatesDto;
	locations: LocationDto[];
	marketRates: MarketRateDto[];
};

export type OfficialRateHistoryPoint = {
	rateDate: string;
	unit: number;
	rate: number;
	fetchedAt: string;
};

export type BestRateOperation = "BUY_FOREIGN_CURRENCY" | "SELL_FOREIGN_CURRENCY";

type QueryValue = number | string | null | undefined;

type RequestOptions = {
	signal?: AbortSignal;
};

function getApiBaseUrl() {
	const localHosts = new Set(["localhost", "127.0.0.1", "::1"]);

	if (localHosts.has(window.location.hostname)) {
		return "http://localhost:8080/api/v1";
	}

	return "/api/v1";
}

function buildUrl(path: string, params: Record<string, QueryValue> = {}) {
	const url = new URL(`${getApiBaseUrl()}${path}`, window.location.origin);

	for (const [key, value] of Object.entries(params)) {
		if (value !== null && value !== undefined && value !== "") {
			url.searchParams.set(key, String(value));
		}
	}

	return url;
}

async function requestJson<T>(path: string, params: Record<string, QueryValue> = {}, options: RequestOptions = {}) {
	const response = await fetch(buildUrl(path, params), {
		headers: {
			Accept: "application/json"
		},
		signal: options.signal
	});

	if (!response.ok) {
		const message = await response.text();
		throw new Error(message || `API returned ${response.status}`);
	}

	return (await response.json()) as T;
}

export function fetchBootstrap(country: string, city: string, locale: string, options?: RequestOptions) {
	return requestJson<BootstrapDto>("/bootstrap", { city, country, locale }, options);
}

export function fetchCountries(locale: string, options?: RequestOptions) {
	return requestJson<CountryDto[]>("/countries", { locale }, options);
}

export function fetchCities(country: string, locale: string, options?: RequestOptions) {
	return requestJson<CityDto[]>(`/countries/${country}/cities`, { locale }, options);
}

export function fetchBestMarketRates(
	country: string,
	city: string,
	currency: string,
	operation: BestRateOperation,
	locale: string,
	options?: RequestOptions
) {
	return requestJson<BestMarketRatesDto>(
		"/market-rates/best",
		{
			city,
			country,
			currency,
			locale,
			operation,
			type: "CASH"
		},
		options
	);
}

export function fetchOfficialRateHistory(country: string, currency: string, from: string, to: string, options?: RequestOptions) {
	return requestJson<OfficialRateHistoryPoint[]>(
		"/official-rates/history",
		{
			country,
			currency,
			from,
			to
		},
		options
	);
}

export function isAbortError(error: unknown) {
	return error instanceof DOMException && error.name === "AbortError";
}
