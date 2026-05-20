package net.magical.exchange.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import net.magical.exchange.repository.MarketRateRepository.MarketRateUpsert;
import net.magical.exchange.repository.OfficialRateRepository.OfficialRateUpsert;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;

class RepositoryBatchWriteTest {

	@Test
	void insertsMarketRatesWithOneBatchCall() {
		NamedParameterJdbcTemplate namedJdbc = mock(NamedParameterJdbcTemplate.class);
		MarketRateRepository repository = new MarketRateRepository(mock(JdbcClient.class), namedJdbc);
		UUID batchId = UUID.randomUUID();
		List<MarketRateUpsert> rates = List.of(
				new MarketRateUpsert("EUR", "CASH", new BigDecimal("19.10"), new BigDecimal("19.35"), null, 1),
				new MarketRateUpsert("USD", "CASH", new BigDecimal("17.80"), new BigDecimal("18.05"), null, 1));
		ArgumentCaptor<SqlParameterSource[]> parameters = ArgumentCaptor.forClass(SqlParameterSource[].class);

		when(namedJdbc.batchUpdate(contains("INSERT INTO market_exchange_rates"), any(SqlParameterSource[].class)))
				.thenReturn(new int[]{1, Statement.SUCCESS_NO_INFO});

		int inserted = repository.insertRates(batchId, rates);

		assertThat(inserted).isEqualTo(2);
		verify(namedJdbc).batchUpdate(contains("INSERT INTO market_exchange_rates"), parameters.capture());
		assertThat(parameters.getValue()).hasSize(2);
		assertThat(parameters.getValue()[0].getValue("batchId")).isEqualTo(batchId);
		assertThat(parameters.getValue()[0].getValue("currency")).isEqualTo("EUR");
		assertThat(parameters.getValue()[1].getValue("currency")).isEqualTo("USD");
	}

	@Test
	void skipsMarketBatchWhenThereAreNoRates() {
		NamedParameterJdbcTemplate namedJdbc = mock(NamedParameterJdbcTemplate.class);
		MarketRateRepository repository = new MarketRateRepository(mock(JdbcClient.class), namedJdbc);

		int inserted = repository.insertRates(UUID.randomUUID(), List.of());

		assertThat(inserted).isZero();
		verifyNoInteractions(namedJdbc);
	}

	@Test
	void upsertsOfficialRatesWithOneBatchCall() {
		NamedParameterJdbcTemplate namedJdbc = mock(NamedParameterJdbcTemplate.class);
		OfficialRateRepository repository = new OfficialRateRepository(mock(JdbcClient.class), namedJdbc);
		UUID sourceId = UUID.randomUUID();
		LocalDate rateDate = LocalDate.parse("2026-05-18");
		List<OfficialRateUpsert> rates = List.of(new OfficialRateUpsert("EUR", rateDate, 1, new BigDecimal("19.35")),
				new OfficialRateUpsert("USD", rateDate, 1, new BigDecimal("18.05")));
		ArgumentCaptor<SqlParameterSource[]> parameters = ArgumentCaptor.forClass(SqlParameterSource[].class);

		when(namedJdbc.batchUpdate(contains("INSERT INTO official_exchange_rates"), any(SqlParameterSource[].class)))
				.thenReturn(new int[]{1, 0});

		int upserted = repository.upsertRates(sourceId, "MD", rates);

		assertThat(upserted).isEqualTo(1);
		verify(namedJdbc).batchUpdate(contains("INSERT INTO official_exchange_rates"), parameters.capture());
		assertThat(parameters.getValue()).hasSize(2);
		assertThat(parameters.getValue()[0].getValue("sourceId")).isEqualTo(sourceId);
		assertThat(parameters.getValue()[0].getValue("country")).isEqualTo("MD");
		assertThat(parameters.getValue()[1].getValue("currency")).isEqualTo("USD");
	}

	@Test
	void skipsOfficialBatchWhenThereAreNoRates() {
		NamedParameterJdbcTemplate namedJdbc = mock(NamedParameterJdbcTemplate.class);
		OfficialRateRepository repository = new OfficialRateRepository(mock(JdbcClient.class), namedJdbc);

		int upserted = repository.upsertRates(UUID.randomUUID(), "MD", List.of());

		assertThat(upserted).isZero();
		verifyNoInteractions(namedJdbc);
	}
}
