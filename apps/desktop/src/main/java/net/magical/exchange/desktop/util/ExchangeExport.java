package net.magical.exchange.desktop.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.magical.exchange.desktop.model.BootstrapDto;
import net.magical.exchange.desktop.model.MarketRateDto;
import net.magical.exchange.desktop.model.OfficialRateHistoryPoint;

public final class ExchangeExport {

	private static final String LINE_SEPARATOR = System.lineSeparator();

	private ExchangeExport() {
	}

	public enum Format {
		CSV("csv"), TXT("txt");

		private final String extension;

		Format(String nextExtension) {
			extension = nextExtension;
		}

		public String extension() {
			return extension;
		}
	}

	public static String marketRates(BootstrapDto bootstrap, List<MarketRateDto> rates, String selection, Format format) {
		if (format == Format.CSV) {
			return marketRatesCsv(bootstrap, rates, selection);
		}

		return marketRatesTxt(bootstrap, rates, selection);
	}

	public static String officialHistory(String countryCode, String countryName, String baseCurrency, String currency, LocalDate from,
			LocalDate to, List<OfficialRateHistoryPoint> history, Format format) {
		if (format == Format.CSV) {
			return officialRateHistoryCsv(countryCode, countryName, baseCurrency, currency, from, to, history);
		}

		return officialRateHistoryTxt(countryCode, countryName, baseCurrency, currency, from, to, history);
	}

	public static String currentRatesFileName(BootstrapDto bootstrap, String selection) {
		return safeFileName("current-rates-" + bootstrap.country().code() + "-" + bootstrap.city().slug() + "-" + selection);
	}

	public static String officialHistoryFileName(String countryCode, String currency, LocalDate from, LocalDate to) {
		return safeFileName("official-history-" + countryCode + "-" + currency + "-" + from + "-" + to);
	}

	private static String marketRatesCsv(BootstrapDto bootstrap, List<MarketRateDto> rates, String selection) {
		Stream<List<?>> rows = rates.stream().map(rate -> marketRateCsvRow(bootstrap, selection, rate));

		return csv(Stream.concat(Stream.of(marketRateCsvHeaders()), rows).toList());
	}

	private static String marketRatesTxt(BootstrapDto bootstrap, List<MarketRateDto> rates, String selection) {
		List<String> entries = rates.stream().map(ExchangeExport::marketRateTxtEntry).toList();

		return String.join(LINE_SEPARATOR + LINE_SEPARATOR,
				Stream.concat(marketRatesTxtHeader(bootstrap, selection).stream(), entries.stream()).toList());
	}

	private static String officialRateHistoryCsv(String countryCode, String countryName, String baseCurrency, String currency,
			LocalDate from, LocalDate to, List<OfficialRateHistoryPoint> history) {
		Stream<List<?>> rows = history.stream()
				.map(point -> historyCsvRow(countryCode, countryName, baseCurrency, currency, from, to, point));

		return csv(Stream.concat(Stream.of(historyCsvHeaders()), rows).toList());
	}

	private static String officialRateHistoryTxt(String countryCode, String countryName, String baseCurrency, String currency,
			LocalDate from, LocalDate to, List<OfficialRateHistoryPoint> history) {
		List<String> entries = history.stream().map(ExchangeExport::historyTxtEntry).toList();
		List<String> header = historyTxtHeader(countryCode, countryName, baseCurrency, currency, from, to);
		Stream<String> paragraphs = Stream.concat(header.stream(), entries.stream());

		return String.join(LINE_SEPARATOR + LINE_SEPARATOR, paragraphs.toList());
	}

	private static List<?> marketRateCsvHeaders() {
		List<String> headers = new ArrayList<>();
		headers.add("country_code");
		headers.add("country");
		headers.add("city_slug");
		headers.add("city");
		headers.add("selection");
		headers.add("institution");
		headers.add("office");
		headers.add("address");
		headers.add("currency");
		headers.add("currency_name");
		headers.add("rate_type");
		headers.add("unit");
		headers.add("buy_rate");
		headers.add("sell_rate");
		headers.add("official_rate");
		headers.add("published_at");
		headers.add("fetched_at");

		return headers;
	}

	private static List<?> marketRateCsvRow(BootstrapDto bootstrap, String selection, MarketRateDto rate) {
		List<Object> row = new ArrayList<>();
		row.add(bootstrap.country().code());
		row.add(bootstrap.country().name());
		row.add(bootstrap.city().slug());
		row.add(bootstrap.city().name());
		row.add(selection);
		row.add(rate.institution().name());
		row.add(rate.location().name());
		row.add(rate.location().address());
		row.add(rate.currency().code());
		row.add(rate.currency().name());
		row.add(rate.rateType());
		row.add(rate.unit());
		row.add(rate.buyRate());
		row.add(rate.sellRate());
		row.add(rate.officialRate());
		row.add(rate.publishedAt());
		row.add(rate.fetchedAt());

		return row;
	}

	private static List<String> marketRatesTxtHeader(BootstrapDto bootstrap, String selection) {
		List<String> header = new ArrayList<>();
		header.add("Current bank exchange rates");
		header.add("Country: " + bootstrap.country().name() + " (" + bootstrap.country().code() + ")");
		header.add("City: " + bootstrap.city().name());
		header.add("Currency selection: " + selection);
		header.add("Generated at: " + bootstrap.generatedAt());

		return header;
	}

	private static String marketRateTxtEntry(MarketRateDto rate) {
		List<String> lines = new ArrayList<>();
		lines.add(rate.institution().name() + " - " + rate.location().name());
		lines.add("Address: " + rate.location().address());
		lines.add("Currency: " + rate.currency().code() + " (" + rate.currency().name() + ")");
		lines.add("Rate type: " + rate.rateType());
		lines.add("Unit: " + rate.unit());
		lines.add("Buy: " + value(rate.buyRate()));
		lines.add("Sell: " + value(rate.sellRate()));
		lines.add("Official rate: " + value(rate.officialRate()));
		lines.add("Published at: " + value(rate.publishedAt()));
		lines.add("Fetched at: " + rate.fetchedAt());

		return String.join(LINE_SEPARATOR, lines);
	}

	private static List<?> historyCsvHeaders() {
		List<String> headers = new ArrayList<>();
		headers.add("country_code");
		headers.add("country");
		headers.add("base_currency");
		headers.add("currency");
		headers.add("from");
		headers.add("to");
		headers.add("rate_date");
		headers.add("unit");
		headers.add("rate");
		headers.add("fetched_at");

		return headers;
	}

	private static List<?> historyCsvRow(String countryCode, String countryName, String baseCurrency, String currency, LocalDate from,
			LocalDate to, OfficialRateHistoryPoint point) {
		List<Object> row = new ArrayList<>();
		row.add(countryCode);
		row.add(countryName);
		row.add(baseCurrency);
		row.add(currency);
		row.add(from);
		row.add(to);
		row.add(point.rateDate());
		row.add(point.unit());
		row.add(point.rate());
		row.add(point.fetchedAt());

		return row;
	}

	private static List<String> historyTxtHeader(String countryCode, String countryName, String baseCurrency, String currency,
			LocalDate from, LocalDate to) {
		List<String> header = new ArrayList<>();
		header.add("Official national-bank rate history");
		header.add("Country: " + countryName + " (" + countryCode + ")");
		header.add("Currency: " + currency + " / " + baseCurrency);
		header.add("Range: " + from + " - " + to);

		return header;
	}

	private static String historyTxtEntry(OfficialRateHistoryPoint point) {
		List<String> lines = new ArrayList<>();
		lines.add("Date: " + point.rateDate());
		lines.add("Unit: " + point.unit());
		lines.add("Rate: " + point.rate());
		lines.add("Fetched at: " + point.fetchedAt());

		return String.join(LINE_SEPARATOR, lines);
	}

	private static String csv(List<List<?>> rows) {
		return rows.stream().map(row -> row.stream().map(ExchangeExport::csvCell).collect(Collectors.joining(",")))
				.collect(Collectors.joining("\n"));
	}

	private static String csvCell(Object value) {
		String text = value(value);

		if (text.contains("\"") || text.contains(",") || text.contains("\n") || text.contains("\r")) {
			return "\"" + text.replace("\"", "\"\"") + "\"";
		}

		return text;
	}

	private static String value(Object value) {
		return Objects.toString(value, "");
	}

	private static String safeFileName(String value) {
		return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]+", "-").replaceAll("(^-+|-+$)", "");
	}
}
