import type { MarketRateDto } from "@/services/exchange-api";
import type { MarketRateGroup } from "./types";

export function groupMarketRates(rates: MarketRateDto[]) {
	const groups = new Map<string, MarketRateGroup>();

	for (const rate of rates) {
		const existingGroup = groups.get(rate.location.id);

		if (existingGroup) {
			existingGroup.rates.push(rate);
		} else {
			groups.set(rate.location.id, {
				location: rate.location,
				rates: [rate]
			});
		}
	}

	return [...groups.values()]
		.map((group) => ({
			...group,
			rates: [...group.rates].sort((left, right) => left.currency.code.localeCompare(right.currency.code))
		}))
		.sort((left, right) => left.location.institution.name.localeCompare(right.location.institution.name));
}
