import type { SelectOption } from "@/app-types";
import type { BootstrapDto, CurrencyDto } from "@/services/exchange-api";

export function getForeignCurrencyOptions(bootstrap: BootstrapDto | null, fallbackCurrency: string): SelectOption[] {
	if (!bootstrap) {
		return [{ label: fallbackCurrency, value: fallbackCurrency }];
	}

	return bootstrap.currencies.filter((currency) => currency.code !== bootstrap.country.baseCurrencyCode).map(currencyOption);
}

function currencyOption(currency: CurrencyDto) {
	return {
		label: `${currency.code} · ${currency.name}`,
		value: currency.code
	};
}
