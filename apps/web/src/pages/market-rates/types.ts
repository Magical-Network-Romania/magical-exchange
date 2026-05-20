import type { LocationDto, MarketRateDto } from "@/services/exchange-api";

export type MarketRateGroup = {
	location: LocationDto;
	rates: MarketRateDto[];
};
