package net.magical.exchange.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.magical.exchange.api.BestRateOperation;
import net.magical.exchange.api.ExchangeDtos.BestMarketRatesDto;
import net.magical.exchange.api.ExchangeDtos.BootstrapDto;
import net.magical.exchange.api.ExchangeDtos.CityDto;
import net.magical.exchange.api.ExchangeDtos.CountryDto;
import net.magical.exchange.api.ExchangeDtos.CurrencyDto;
import net.magical.exchange.api.ExchangeDtos.LocationDto;
import net.magical.exchange.api.ExchangeDtos.MarketRateDto;
import net.magical.exchange.api.ExchangeDtos.OfficialRateHistoryPoint;
import net.magical.exchange.api.ExchangeDtos.OfficialRatesDto;
import net.magical.exchange.repository.CityRepository;
import net.magical.exchange.repository.CityRepository.CityRecord;
import net.magical.exchange.repository.CountryRepository;
import net.magical.exchange.repository.CountryRepository.CountryRecord;
import net.magical.exchange.repository.CurrencyRepository;
import net.magical.exchange.repository.CurrencyRepository.CurrencyRecord;
import net.magical.exchange.repository.LocationRepository;
import net.magical.exchange.repository.MarketRateRepository;
import net.magical.exchange.repository.MarketRateRepository.MarketRateRecord;
import net.magical.exchange.repository.OfficialRateRepository;
import net.magical.exchange.repository.OfficialRateRepository.OfficialRateRecord;
import net.magical.exchange.service.exception.BadRequestException;
import net.magical.exchange.service.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ExchangeQueryService {

	private static final Pattern COUNTRY_PATTERN = Pattern.compile("^[A-Z]{2}$");
	private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
	private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]+$");

	private final CityRepository cityRepository;
	private final CountryRepository countryRepository;
	private final CurrencyRepository currencyRepository;
	private final ExchangeLocalizationService localizationService;
	private final LocationRepository locationRepository;
	private final MarketRateRepository marketRateRepository;
	private final OfficialRateRepository officialRateRepository;

	public ExchangeQueryService(CountryRepository countries, CityRepository cities, CurrencyRepository currencies,
			LocationRepository locations, OfficialRateRepository officialRates, MarketRateRepository marketRates,
			ExchangeLocalizationService localizationService) {
		this.countryRepository = countries;
		this.cityRepository = cities;
		this.currencyRepository = currencies;
		this.locationRepository = locations;
		this.officialRateRepository = officialRates;
		this.marketRateRepository = marketRates;
		this.localizationService = localizationService;
	}

	public List<CountryDto> findCountries(String locale) {
		return localizationService.localizeCountries(countryRepository.findAllActive(), locale);
	}

	public List<CityDto> findCities(String countryCode, String locale) {
		String country = normalizeCountry(countryCode);

		ensureCountry(country);

		return localizationService.localizeCities(country, cityRepository.findActiveByCountry(country), locale);
	}

	public List<CurrencyDto> findCurrencies(String locale) {
		return localizationService.localizeCurrencies(currencyRepository.findAllActive(), locale);
	}

	public OfficialRatesDto findOfficialRates(String countryCode, LocalDate date, String locale) {
		String country = normalizeCountry(countryCode);
		CountryRecord countryRecord = ensureCountry(country);
		LocalDate rateDate = date == null ? officialRateRepository.findLatestDate(country) : date;

		if (rateDate == null) {
			return new OfficialRatesDto(country, countryRecord.baseCurrencyCode(), null, List.of());
		}

		List<OfficialRateRecord> rates = officialRateRepository.findByCountryAndDate(country, rateDate);
		List<CurrencyRecord> currencies = currencyRepository.findByCodes(officialCurrencyCodes(rates));

		return new OfficialRatesDto(country, countryRecord.baseCurrencyCode(), rateDate,
				localizationService.officialRates(rates, currencies, locale));
	}

	public List<OfficialRateHistoryPoint> findOfficialRateHistory(String country, String currency, LocalDate from, LocalDate to) {
		String normalizedCountry = normalizeCountry(country);
		String normalizedCurrency = normalizeCurrency(currency);

		validateDateRange(from, to);

		return officialRateRepository.findHistory(normalizedCountry, normalizedCurrency, from, to);
	}

	public List<LocationDto> findLocations(String countryCode, String citySlug) {
		String country = normalizeCountry(countryCode);
		String city = normalizeSlug(citySlug, "city");

		ensureCity(country, city);

		return localizationService.locations(locationRepository.findByCountryAndCity(country, city));
	}

	public List<MarketRateDto> findMarketRates(String country, String citySlug, String currencyCode, String rateType, String locale) {
		String normalizedCountry = normalizeCountry(country);
		String city = normalizeSlug(citySlug, "city");
		String currency = currencyCode == null || currencyCode.isBlank() ? null : normalizeCurrency(currencyCode);
		String normalizedRateType = normalizeRateType(rateType);

		ensureCity(normalizedCountry, city);

		return queryMarketRates(normalizedCountry, city, currency, normalizedRateType, locale);
	}

	public BestMarketRatesDto findBestMarketRates(String countryCode, String citySlug, String currencyCode, BestRateOperation operation,
			String rateType, String locale) {
		String country = normalizeCountry(countryCode);
		String city = normalizeSlug(citySlug, "city");
		String currency = normalizeCurrency(currencyCode);
		String normalizedRateType = normalizeRateType(rateType);

		ensureCity(country, city);

		List<MarketRateDto> marketRates = queryMarketRates(country, city, currency, normalizedRateType, locale).stream()
				.filter(rate -> hasOperationRate(rate, operation)).sorted(bestRateComparator(operation)).toList();

		return new BestMarketRatesDto(country, city, currency, normalizedRateType, operation.name(), marketRates);
	}

	public BootstrapDto bootstrap(String countryCode, String citySlug, String locale) {
		String country = normalizeCountry(countryCode);
		String city = normalizeSlug(citySlug, "city");
		CountryDto countryResponse = localizationService.localizeCountry(ensureCountry(country), locale);
		CityDto cityResponse = localizationService.localizeCity(ensureCity(country, city), locale);

		return new BootstrapDto(Instant.now(), countryResponse, cityResponse, findCurrencies(locale),
				findOfficialRates(country, null, locale), findLocations(country, city),
				queryMarketRates(country, city, null, "CASH", locale));
	}

	private List<MarketRateDto> queryMarketRates(String country, String city, String currency, String rateType, String locale) {
		List<MarketRateRecord> rates = marketRateRepository.findLatestByCity(country, city, currency, rateType);
		List<CurrencyRecord> currencies = currencyRepository.findByCodes(marketCurrencyCodes(rates));

		return localizationService.marketRates(rates, currencies, locale);
	}

	private CountryRecord ensureCountry(String country) {
		return countryRepository.findActiveByCode(country).orElseThrow(() -> countryNotFound(country));
	}

	private NotFoundException countryNotFound(String country) {
		return new NotFoundException("Country not found: " + country);
	}

	private CityRecord ensureCity(String country, String city) {
		return cityRepository.findActiveByCountryAndSlug(country, city)
				.orElseThrow(() -> new NotFoundException("City not found: " + country + "/" + city));
	}

	private List<String> officialCurrencyCodes(Collection<OfficialRateRecord> rates) {
		return rates.stream().map(OfficialRateRecord::currencyCode).distinct().toList();
	}

	private List<String> marketCurrencyCodes(Collection<MarketRateRecord> rates) {
		return rates.stream().map(MarketRateRecord::currencyCode).distinct().toList();
	}

	private String normalizeCountry(String countryCode) {
		String country = normalizeRequired(countryCode, "country").toUpperCase(Locale.ROOT);

		if (!COUNTRY_PATTERN.matcher(country).matches()) {
			throw new BadRequestException("Invalid country code: " + countryCode);
		}

		return country;
	}

	private String normalizeCurrency(String currencyCode) {
		String currency = normalizeRequired(currencyCode, "currency").toUpperCase(Locale.ROOT);

		if (!CURRENCY_PATTERN.matcher(currency).matches()) {
			throw new BadRequestException("Invalid currency code: " + currencyCode);
		}

		return currency;
	}

	private String normalizeSlug(String value, String fieldName) {
		String slug = normalizeRequired(value, fieldName).toLowerCase(Locale.ROOT);

		if (!SLUG_PATTERN.matcher(slug).matches()) {
			throw new BadRequestException("Invalid " + fieldName + ": " + value);
		}

		return slug;
	}

	private String normalizeRateType(String rateType) {
		String normalizedRateType = normalizeRequired(rateType, "type").toUpperCase(Locale.ROOT);

		if (!List.of("CASH", "CARD", "ATM").contains(normalizedRateType)) {
			throw new BadRequestException("Unsupported rate type: " + rateType);
		}

		return normalizedRateType;
	}

	private String normalizeRequired(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new BadRequestException(fieldName + " is required");
		}

		return value.trim();
	}

	private void validateDateRange(LocalDate from, LocalDate to) {
		if (from == null || to == null) {
			throw new BadRequestException("from and to are required");
		}

		if (to.isBefore(from)) {
			throw new BadRequestException("to must be on or after from");
		}
	}

	private boolean hasOperationRate(MarketRateDto rate, BestRateOperation operation) {
		return switch (operation) {
			case BUY_FOREIGN_CURRENCY -> rate.sellRate() != null;
			case SELL_FOREIGN_CURRENCY -> rate.buyRate() != null;
		};
	}

	private Comparator<MarketRateDto> bestRateComparator(BestRateOperation operation) {
		return switch (operation) {
			case BUY_FOREIGN_CURRENCY -> Comparator.comparing(MarketRateDto::sellRate);
			case SELL_FOREIGN_CURRENCY -> Comparator.comparing(MarketRateDto::buyRate).reversed();
		};
	}
}
