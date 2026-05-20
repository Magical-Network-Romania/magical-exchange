package net.magical.exchange.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.magical.exchange.api.ExchangeDtos.CityDto;
import net.magical.exchange.api.ExchangeDtos.CountryDto;
import net.magical.exchange.api.ExchangeDtos.CurrencyDto;
import net.magical.exchange.api.ExchangeDtos.InstitutionDto;
import net.magical.exchange.api.ExchangeDtos.LocationDto;
import net.magical.exchange.api.ExchangeDtos.MarketRateDto;
import net.magical.exchange.api.ExchangeDtos.OfficialRateDto;
import net.magical.exchange.repository.CityRepository.CityRecord;
import net.magical.exchange.repository.CountryRepository.CountryRecord;
import net.magical.exchange.repository.CurrencyRepository.CurrencyRecord;
import net.magical.exchange.repository.LocalizationRepository;
import net.magical.exchange.repository.LocationRepository.InstitutionRecord;
import net.magical.exchange.repository.LocationRepository.LocationRecord;
import net.magical.exchange.repository.MarketRateRepository.MarketRateRecord;
import net.magical.exchange.repository.OfficialRateRepository.OfficialRateRecord;
import org.springframework.stereotype.Service;

@Service
public class ExchangeLocalizationService {

	private final LocalizationRepository localizationRepository;

	public ExchangeLocalizationService(LocalizationRepository localizationRepository) {
		this.localizationRepository = localizationRepository;
	}

	public List<CountryDto> localizeCountries(List<CountryRecord> countries, String locale) {
		Map<String, String> names = localizationRepository.findCountryNames(countryCodes(countries), locale);

		return countries.stream().map(country -> localizeCountry(country, names)).sorted(countryOrder()).toList();
	}

	public CountryDto localizeCountry(CountryRecord country, String locale) {
		Map<String, String> names = localizationRepository.findCountryNames(List.of(country.code()), locale);

		return localizeCountry(country, names);
	}

	public List<CityDto> localizeCities(String country, List<CityRecord> cities, String locale) {
		Map<String, String> names = localizationRepository.findCityNames(country, citySlugs(cities), locale);

		return cities.stream().map(city -> localizeCity(city, names)).sorted(cityOrder()).toList();
	}

	public CityDto localizeCity(CityRecord city, String locale) {
		Map<String, String> names = localizationRepository.findCityNames(city.countryCode(), List.of(city.slug()), locale);

		return localizeCity(city, names);
	}

	public List<CurrencyDto> localizeCurrencies(List<CurrencyRecord> currencies, String locale) {
		Map<String, String> names = localizationRepository.findCurrencyNames(currencyCodes(currencies), locale);

		return currencies.stream().map(currency -> localizeCurrency(currency, names)).toList();
	}

	public List<LocationDto> locations(List<LocationRecord> locations) {
		return locations.stream().map(this::location).toList();
	}

	public List<OfficialRateDto> officialRates(List<OfficialRateRecord> rates, List<CurrencyRecord> currencies, String locale) {
		Map<String, CurrencyDto> currenciesByCode = localizeCurrenciesByCode(currencies, locale);

		return rates.stream().map(rate -> officialRate(rate, currenciesByCode)).toList();
	}

	public List<MarketRateDto> marketRates(List<MarketRateRecord> rates, List<CurrencyRecord> currencies, String locale) {
		Map<String, CurrencyDto> currenciesByCode = localizeCurrenciesByCode(currencies, locale);

		return rates.stream().map(rate -> marketRate(rate, currenciesByCode)).toList();
	}

	private CountryDto localizeCountry(CountryRecord country, Map<String, String> names) {
		String name = names.getOrDefault(country.code(), country.code());

		return new CountryDto(country.code(), name, country.baseCurrencyCode(), country.defaultLocaleCode());
	}

	private CityDto localizeCity(CityRecord city, Map<String, String> names) {
		String name = names.getOrDefault(city.slug(), city.slug());

		return new CityDto(city.countryCode(), city.slug(), name, city.timezone());
	}

	private CurrencyDto localizeCurrency(CurrencyRecord currency, Map<String, String> names) {
		String name = names.getOrDefault(currency.code(), currency.code());

		return new CurrencyDto(currency.code(), name, currency.numericCode(), currency.minorUnits());
	}

	private Map<String, CurrencyDto> localizeCurrenciesByCode(List<CurrencyRecord> currencies, String locale) {
		return localizeCurrencies(currencies, locale).stream().collect(Collectors.toMap(CurrencyDto::code, Function.identity()));
	}

	private OfficialRateDto officialRate(OfficialRateRecord rate, Map<String, CurrencyDto> currenciesByCode) {
		CurrencyDto currency = requireCurrency(rate.currencyCode(), currenciesByCode);

		return new OfficialRateDto(currency, rate.unit(), rate.rate(), rate.sourceSlug(), rate.fetchedAt());
	}

	private MarketRateDto marketRate(MarketRateRecord rate, Map<String, CurrencyDto> currenciesByCode) {
		CurrencyDto currency = requireCurrency(rate.currencyCode(), currenciesByCode);
		InstitutionDto institution = institution(rate.institution());
		LocationDto location = location(rate.location());

		return new MarketRateDto(currency, rate.type(), rate.buy(), rate.sell(), rate.official(), rate.unit(), rate.fetchedAt(),
				rate.publishedAt(), institution, location);
	}

	private LocationDto location(LocationRecord location) {
		InstitutionDto institution = institution(location.institution());
		String country = location.countryCode();
		String city = location.citySlug();
		String address = location.address();
		BigDecimal latitude = location.latitude();
		BigDecimal longitude = location.longitude();

		return new LocationDto(location.id(), country, city, location.slug(), location.name(), address, latitude, longitude,
				location.phone(), location.email(), institution);
	}

	private InstitutionDto institution(InstitutionRecord institution) {
		String websiteUrl = institution.websiteUrl();

		return new InstitutionDto(institution.id(), institution.slug(), institution.name(), institution.type(), websiteUrl);
	}

	private CurrencyDto requireCurrency(String currencyCode, Map<String, CurrencyDto> currenciesByCode) {
		CurrencyDto currency = currenciesByCode.get(currencyCode);

		if (currency == null) {
			throw new IllegalStateException("Currency not found: " + currencyCode);
		}

		return currency;
	}

	private List<String> countryCodes(List<CountryRecord> countries) {
		return countries.stream().map(CountryRecord::code).toList();
	}

	private List<String> citySlugs(List<CityRecord> cities) {
		return cities.stream().map(CityRecord::slug).toList();
	}

	private List<String> currencyCodes(Collection<CurrencyRecord> currencies) {
		return currencies.stream().map(CurrencyRecord::code).toList();
	}

	private Comparator<CountryDto> countryOrder() {
		return Comparator.comparing(CountryDto::name).thenComparing(CountryDto::code);
	}

	private Comparator<CityDto> cityOrder() {
		return Comparator.comparing(CityDto::name).thenComparing(CityDto::slug);
	}
}
