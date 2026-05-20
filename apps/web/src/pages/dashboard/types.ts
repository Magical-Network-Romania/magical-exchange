import type { MarketRateDto } from "@/services/exchange-api";

export type RateOfferKind = "buy" | "sell";

export type RateOffer = {
	rate: MarketRateDto;
	value: number;
};
