package net.magical.exchange.desktop.model;

import java.time.Instant;
import java.util.List;

public record BootstrapDto(Instant generatedAt, CountryDto country, CityDto city, List<CurrencyDto> currencies,
		OfficialRatesDto officialRates, List<LocationDto> locations, List<MarketRateDto> marketRates) {

	public BootstrapDto {
		currencies = List.copyOf(currencies);
		locations = List.copyOf(locations);
		marketRates = List.copyOf(marketRates);
	}
}
