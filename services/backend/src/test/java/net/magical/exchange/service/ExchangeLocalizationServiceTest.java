package net.magical.exchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.magical.exchange.api.ExchangeDtos.CountryDto;
import net.magical.exchange.api.ExchangeDtos.CurrencyDto;
import net.magical.exchange.api.ExchangeDtos.MarketRateDto;
import net.magical.exchange.repository.CountryRepository.CountryRecord;
import net.magical.exchange.repository.CurrencyRepository.CurrencyRecord;
import net.magical.exchange.repository.LocalizationRepository;
import net.magical.exchange.repository.LocationRepository.InstitutionRecord;
import net.magical.exchange.repository.LocationRepository.LocationRecord;
import net.magical.exchange.repository.MarketRateRepository.MarketRateRecord;
import org.junit.jupiter.api.Test;

class ExchangeLocalizationServiceTest {

	@Test
	void localizesAndSortsCountriesByTranslatedName() {
		LocalizationRepository names = mock(LocalizationRepository.class);
		ExchangeLocalizationService service = new ExchangeLocalizationService(names);
		List<CountryRecord> countries = List.of(new CountryRecord("ZZ", "ZZZ", "en"), new CountryRecord("AA", "AAA", "en"));

		when(names.findCountryNames(List.of("ZZ", "AA"), "ro")).thenReturn(Map.of("ZZ", "Zeta", "AA", "Alfa"));

		List<CountryDto> localized = service.localizeCountries(countries, "ro");

		assertThat(localized).extracting(CountryDto::code).containsExactly("AA", "ZZ");
		assertThat(localized).extracting(CountryDto::name).containsExactly("Alfa", "Zeta");
	}

	@Test
	void fallsBackToCurrencyCodeWhenNameIsMissing() {
		LocalizationRepository names = mock(LocalizationRepository.class);
		ExchangeLocalizationService service = new ExchangeLocalizationService(names);
		List<CurrencyRecord> currencies = List.of(new CurrencyRecord("EUR", "978", 2));

		when(names.findCurrencyNames(List.of("EUR"), "ro")).thenReturn(Map.of());

		List<CurrencyDto> localized = service.localizeCurrencies(currencies, "ro");

		assertThat(localized).containsExactly(new CurrencyDto("EUR", "EUR", "978", 2));
	}

	@Test
	void localizesMarketRateCurrencyWithoutChangingMainRateData() {
		LocalizationRepository names = mock(LocalizationRepository.class);
		ExchangeLocalizationService service = new ExchangeLocalizationService(names);
		UUID institutionId = UUID.randomUUID();
		UUID locationId = UUID.randomUUID();
		InstitutionRecord institution = new InstitutionRecord(institutionId, "bank", "Bank", "BANK", "https://example.test");
		LocationRecord location = new LocationRecord(locationId, "MD", "chisinau", "center", "Center", "Address", BigDecimal.ONE,
				BigDecimal.TEN, "123", "mail@example.test", institution);
		OffsetDateTime fetchedAt = OffsetDateTime.parse("2026-05-18T12:00:00+03:00");
		BigDecimal buy = new BigDecimal("19.10");
		BigDecimal sell = new BigDecimal("19.35");
		MarketRateRecord rate = new MarketRateRecord("EUR", "CASH", buy, sell, null, 1, fetchedAt, null, institution, location);
		List<CurrencyRecord> currencies = List.of(new CurrencyRecord("EUR", "978", 2));

		when(names.findCurrencyNames(List.of("EUR"), "ro")).thenReturn(Map.of("EUR", "Euro"));

		List<MarketRateDto> localized = service.marketRates(List.of(rate), currencies, "ro");

		assertThat(localized).hasSize(1);
		assertThat(localized.get(0).currency()).isEqualTo(new CurrencyDto("EUR", "Euro", "978", 2));
		assertThat(localized.get(0).buyRate()).isEqualByComparingTo("19.10");
		assertThat(localized.get(0).location().id()).isEqualTo(locationId);
	}

	@Test
	void fallsBackToCurrencyCodeForMarketRatesWhenCurrencyMetadataIsMissing() {
		LocalizationRepository names = mock(LocalizationRepository.class);
		ExchangeLocalizationService service = new ExchangeLocalizationService(names);
		UUID institutionId = UUID.randomUUID();
		UUID locationId = UUID.randomUUID();
		InstitutionRecord institution = new InstitutionRecord(institutionId, "bank", "Bank", "BANK", "https://example.test");
		LocationRecord location = new LocationRecord(locationId, "MD", "chisinau", "center", "Center", "Address", BigDecimal.ONE,
				BigDecimal.TEN, "123", "mail@example.test", institution);
		MarketRateRecord rate = new MarketRateRecord("JPY", "CASH", BigDecimal.ONE, BigDecimal.TEN, null, 100,
				OffsetDateTime.parse("2026-05-18T12:00:00+03:00"), null, institution, location);

		List<MarketRateDto> localized = service.marketRates(List.of(rate), List.of(), "ro");

		assertThat(localized).hasSize(1);
		assertThat(localized.get(0).currency()).isEqualTo(new CurrencyDto("JPY", "JPY", null, 2));
	}
}
