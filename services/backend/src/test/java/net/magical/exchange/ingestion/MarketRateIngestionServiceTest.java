package net.magical.exchange.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import net.magical.exchange.repository.MarketRateRepository.MarketRateUpsert;
import net.magical.exchange.repository.RateSourceRepository.MarketRateSource;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

class MarketRateIngestionServiceTest {

	private final MarketRateIngestionService service = new MarketRateIngestionService(null, null, null, null,
			mock(PlatformTransactionManager.class));

	@Test
	void parsesRowsWithCurrencyLabelsBetweenRates() {
		MarketRateSource source = source("fincombank-md-cash");
		String html = """
				Valuta Cumpărăm Vindem
				EUR 20.08 MDL 20.25 MDL
				USD 17.28 RON 17.46 RON
				GBP 23.08 MDL 23.35 MDL
				""";

		List<MarketRateUpsert> rates = service.parseMarketRates(source, html, activeCurrencies());

		assertThat(rates).extracting(MarketRateUpsert::currency).containsExactly("EUR", "USD", "GBP");
		assertThat(rateFor(rates, "EUR").buy()).isEqualByComparingTo(new BigDecimal("20.08"));
		assertThat(rateFor(rates, "EUR").sell()).isEqualByComparingTo(new BigDecimal("20.25"));
	}

	@Test
	void parsesIndexedCurrencyTableWithOfficialRates() {
		MarketRateSource source = source("moldindconbank-md-cash");
		String html = """
				Valuta Cumpărare Vânzare Curs BNM
				€ EUR $ USD £ GBP L RON ₴ UAH ₣ CHF ₽ RUB
				20.07 20.27 20.1246
				17.25 17.46 17.3174
				22.80 23.30 23.2139
				3.78 3.91 3.8481
				– – 0.3921
				21.60 22.10 22.0016
				– – 0.2439
				Valuta Cumpărare Vânzare
				""";

		List<MarketRateUpsert> rates = service.parseMarketRates(source, html, activeCurrencies());

		assertThat(rates).extracting(MarketRateUpsert::currency).containsExactly("EUR", "USD", "GBP", "RON", "CHF");
		assertThat(rateFor(rates, "USD").buy()).isEqualByComparingTo(new BigDecimal("17.25"));
		assertThat(rateFor(rates, "USD").sell()).isEqualByComparingTo(new BigDecimal("17.46"));
		assertThat(rateFor(rates, "USD").official()).isEqualByComparingTo(new BigDecimal("17.3174"));
	}

	@Test
	void parsesEuroCreditRowsWithOfficialSalePurchaseOrder() {
		MarketRateSource source = source("eurocreditbank-md-cash", "eurocreditbank-html");
		String html = """
				Currency BNM Sale Purchase
				EUR 20.1168 20.2400 20.0400
				USD 16.9291 17.1000 16.8600
				""";

		List<MarketRateUpsert> rates = service.parseMarketRates(source, html, activeCurrencies());

		assertThat(rateFor(rates, "EUR").official()).isEqualByComparingTo(new BigDecimal("20.1168"));
		assertThat(rateFor(rates, "EUR").sell()).isEqualByComparingTo(new BigDecimal("20.2400"));
		assertThat(rateFor(rates, "EUR").buy()).isEqualByComparingTo(new BigDecimal("20.0400"));
	}

	@Test
	void parsesComertbankPublishedDateFromSourcePage() {
		MarketRateSource source = source("comertbank-md-cash", "comertbank-html");
		String html = """
				<div class="cur_date">Curs valutar 21.05.2026</div>
				<table>
					<tr><td>USD</td><td>17,25</td><td>17,45</td><td>17,3597</td></tr>
					<tr><td>EUR</td><td>20.13</td><td>20.25</td><td>20.139</td></tr>
				</table>
				""";

		MarketRateIngestionService.MarketRateParseResult result = service.parseMarketRatePage(source, html, activeCurrencies());

		assertThat(result.sourcePublishedDate()).isEqualTo(LocalDate.parse("2026-05-21"));
		assertThat(result.rates()).extracting(MarketRateUpsert::currency).containsExactly("USD", "EUR");
		assertThat(rateFor(result.rates(), "USD").buy()).isEqualByComparingTo(new BigDecimal("17.25"));
	}

	private List<String> activeCurrencies() {
		return List.of("MDL", "EUR", "USD", "GBP", "RON", "UAH", "CHF", "RUB");
	}

	private MarketRateUpsert rateFor(List<MarketRateUpsert> rates, String currency) {
		return rates.stream().filter(rate -> currency.equals(rate.currency())).findFirst().orElseThrow();
	}

	private MarketRateSource source(String slug) {
		return source(slug, "generic-html");
	}

	private MarketRateSource source(String slug, String parserKey) {
		return new MarketRateSource(UUID.randomUUID(), UUID.randomUUID(), null, "MD", null, slug, parserKey, "https://example.test",
				"CASH");
	}
}
