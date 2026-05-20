package net.magical.exchange.api;

import java.time.LocalDate;
import java.util.List;
import net.magical.exchange.api.ExchangeDtos.BestMarketRatesDto;
import net.magical.exchange.api.ExchangeDtos.BootstrapDto;
import net.magical.exchange.api.ExchangeDtos.CityDto;
import net.magical.exchange.api.ExchangeDtos.CountryDto;
import net.magical.exchange.api.ExchangeDtos.CurrencyDto;
import net.magical.exchange.api.ExchangeDtos.LocationDto;
import net.magical.exchange.api.ExchangeDtos.MarketRateDto;
import net.magical.exchange.api.ExchangeDtos.OfficialRateHistoryPoint;
import net.magical.exchange.api.ExchangeDtos.OfficialRatesDto;
import net.magical.exchange.service.ApiLocaleService;
import net.magical.exchange.service.ExchangeQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://[::1]:3000"}, allowedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class ExchangeApiController {

	private final ApiLocaleService apiLocaleService;
	private final ExchangeQueryService exchangeQueryService;

	public ExchangeApiController(ApiLocaleService apiLocaleService, ExchangeQueryService exchangeQueryService) {
		this.apiLocaleService = apiLocaleService;
		this.exchangeQueryService = exchangeQueryService;
	}

	@GetMapping("/countries")
	public List<CountryDto> countries(@RequestParam(required = false) String locale,
			@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
		return exchangeQueryService.findCountries(resolveLocale(locale, acceptLanguage));
	}

	@GetMapping("/countries/{countryCode}/cities")
	public List<CityDto> cities(@PathVariable String countryCode, @RequestParam(required = false) String locale,
			@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
		return exchangeQueryService.findCities(countryCode, resolveLocale(locale, acceptLanguage));
	}

	@GetMapping("/currencies")
	public List<CurrencyDto> currencies(@RequestParam(required = false) String locale,
			@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
		return exchangeQueryService.findCurrencies(resolveLocale(locale, acceptLanguage));
	}

	@GetMapping("/official-rates")
	public OfficialRatesDto officialRates(@RequestParam String country,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) String locale,
			@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
		return exchangeQueryService.findOfficialRates(country, date, resolveLocale(locale, acceptLanguage));
	}

	@GetMapping("/official-rates/history")
	public List<OfficialRateHistoryPoint> officialRateHistory(@RequestParam String country, @RequestParam String currency,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return exchangeQueryService.findOfficialRateHistory(country, currency, from, to);
	}

	@GetMapping("/locations")
	public List<LocationDto> locations(@RequestParam String country, @RequestParam String city) {
		return exchangeQueryService.findLocations(country, city);
	}

	@GetMapping("/market-rates")
	public List<MarketRateDto> marketRates(@RequestParam String country, @RequestParam String city,
			@RequestParam(required = false) String currency, @RequestParam(required = false, defaultValue = "CASH") String type,
			@RequestParam(required = false) String locale,
			@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
		return exchangeQueryService.findMarketRates(country, city, currency, type, resolveLocale(locale, acceptLanguage));
	}

	@GetMapping("/market-rates/best")
	public BestMarketRatesDto bestMarketRates(@RequestParam String country, @RequestParam String city, @RequestParam String currency,
			@RequestParam String operation, @RequestParam(required = false, defaultValue = "CASH") String type,
			@RequestParam(required = false) String locale,
			@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
		String resolvedLocale = resolveLocale(locale, acceptLanguage);
		BestRateOperation bestRateOperation = BestRateOperation.parse(operation);

		return exchangeQueryService.findBestMarketRates(country, city, currency, bestRateOperation, type, resolvedLocale);
	}

	@GetMapping("/bootstrap")
	public BootstrapDto boot(@RequestParam String country, @RequestParam String city, @RequestParam(required = false) String locale,
			@RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
		return exchangeQueryService.bootstrap(country, city, resolveLocale(locale, acceptLanguage));
	}

	private String resolveLocale(String locale, String acceptLanguage) {
		return apiLocaleService.resolve(locale, acceptLanguage);
	}
}
