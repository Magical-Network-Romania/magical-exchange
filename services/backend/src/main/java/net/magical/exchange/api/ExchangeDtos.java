package net.magical.exchange.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ExchangeDtos {

	private ExchangeDtos() {
	}

	public record LocaleResponse(String code, String languageCode, String regionCode, String nativeName) {
	}

	public record CurrencyDto(String code, String name, String numericCode, int minorUnits) {
	}

	public record CountryDto(String code, String name, String baseCurrencyCode, String defaultLocaleCode) {
	}

	public record CityDto(String countryCode, String slug, String name, String timezone) {
	}

	public record InstitutionDto(UUID id, String slug, String name, String type, String websiteUrl) {
	}

	public record LocationDto(UUID id, String countryCode, String citySlug, String slug, String name, String address, BigDecimal lat,
			BigDecimal lng, String phone, String email, InstitutionDto institution) {
	}

	public record OfficialRateDto(CurrencyDto currency, int unit, BigDecimal rate, String source, OffsetDateTime fetchedAt) {
	}

	public record OfficialRatesDto(String country, String baseCurrency, LocalDate rateDate, List<OfficialRateDto> rates) {
		public OfficialRatesDto {
			rates = List.copyOf(rates);
		}
	}

	public record OfficialRateHistoryPoint(LocalDate rateDate, int unit, BigDecimal rate, OffsetDateTime fetchedAt) {
	}

	public record MarketRateDto(CurrencyDto currency, String rateType, BigDecimal buyRate, BigDecimal sellRate, BigDecimal officialRate,
			int unit, OffsetDateTime fetchedAt, OffsetDateTime publishedAt, InstitutionDto institution, LocationDto location) {
	}

	public record BestMarketRatesDto(String countryCode, String citySlug, String currencyCode, String rateType, String operation,
			List<MarketRateDto> rates) {
		public BestMarketRatesDto {
			rates = List.copyOf(rates);
		}
	}

	public record BootstrapDto(Instant generatedAt, CountryDto country, CityDto city, List<CurrencyDto> currencies,
			OfficialRatesDto officialRates, List<LocationDto> locations, List<MarketRateDto> marketRates) {
		public BootstrapDto {
			currencies = List.copyOf(currencies);
			locations = List.copyOf(locations);
			marketRates = List.copyOf(marketRates);
		}
	}
}
