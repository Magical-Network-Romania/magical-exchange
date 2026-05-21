package net.magical.exchange.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import net.magical.exchange.repository.CurrencyRepository;
import net.magical.exchange.repository.MarketRateRepository;
import net.magical.exchange.repository.MarketRateRepository.MarketRateUpsert;
import net.magical.exchange.repository.OfficialRateRepository;
import net.magical.exchange.repository.OfficialRateRepository.OfficialRateUpsert;
import net.magical.exchange.repository.RateSourceRepository;
import net.magical.exchange.repository.RateSourceRepository.MarketRateSource;
import net.magical.exchange.repository.RateSourceRepository.OfficialRateSource;
import net.magical.exchange.repository.SourceFetchRunRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

class IngestionTransactionTest {

	@Test
	void rollsBackMarketWritesWhenRateInsertFails() {
		RateSourceRepository rateSources = mock(RateSourceRepository.class);
		SourceFetchRunRepository runs = mock(SourceFetchRunRepository.class);
		MarketRateRepository marketRates = mock(MarketRateRepository.class);
		TransactionMocks transactions = transactionMocks();
		CurrencyRepository currencies = mock(CurrencyRepository.class);
		MarketRateIngestionService service = new MarketRateIngestionService(rateSources, runs, currencies, marketRates,
				transactions.manager());
		UUID runId = UUID.randomUUID();
		UUID sourceId = UUID.randomUUID();
		UUID batchId = UUID.randomUUID();
		String parserKey = "generic-html";
		MarketRateSource source = new MarketRateSource(sourceId, UUID.randomUUID(), null, "MD", "chisinau", "source", parserKey,
				"https://example.test", "CASH");
		List<MarketRateUpsert> rates = List
				.of(new MarketRateUpsert("EUR", "CASH", new BigDecimal("19.10"), new BigDecimal("19.35"), null, 1));
		DataIntegrityViolationException failure = new DataIntegrityViolationException("bad rate");

		when(marketRates.insertBatch(source, (OffsetDateTime) null, "checksum")).thenReturn(batchId);
		when(marketRates.insertRates(batchId, rates)).thenThrow(failure);

		assertThatThrownBy(() -> service.saveFetchedRates(runId, source, "checksum", rates)).isSameAs(failure);

		verify(transactions.manager()).rollback(transactions.status());
		verify(transactions.manager(), never()).commit(any(TransactionStatus.class));
		verify(runs, never()).finish(eq(runId), eq("SUCCESS"), anyInt(), any());
		verify(rateSources, never()).markFetched(sourceId);
	}

	@Test
	void commitsMarketWritesWhenRatesAreUnchanged() {
		RateSourceRepository rateSources = mock(RateSourceRepository.class);
		SourceFetchRunRepository runs = mock(SourceFetchRunRepository.class);
		MarketRateRepository marketRates = mock(MarketRateRepository.class);
		TransactionMocks transactions = transactionMocks();
		CurrencyRepository currencies = mock(CurrencyRepository.class);
		MarketRateIngestionService service = new MarketRateIngestionService(rateSources, runs, currencies, marketRates,
				transactions.manager());
		UUID runId = UUID.randomUUID();
		UUID sourceId = UUID.randomUUID();
		String parserKey = "generic-html";
		MarketRateSource source = new MarketRateSource(sourceId, UUID.randomUUID(), null, "MD", "chisinau", "source", parserKey,
				"https://example.test", "CASH");
		List<MarketRateUpsert> rates = List
				.of(new MarketRateUpsert("EUR", "CASH", new BigDecimal("19.10"), new BigDecimal("19.35"), null, 1));

		when(marketRates.insertBatch(source, (OffsetDateTime) null, "checksum")).thenReturn(null);

		int inserted = service.saveFetchedRates(runId, source, "checksum", rates);

		assertThat(inserted).isZero();
		verify(runs).finish(runId, "SKIPPED", 0, "Market rates unchanged");
		verify(rateSources).markFetched(sourceId);
		verify(transactions.manager()).commit(transactions.status());
		verify(transactions.manager(), never()).rollback(any(TransactionStatus.class));
	}

	@Test
	void rollsBackOfficialWritesWhenUpsertFails() {
		RateSourceRepository rateSources = mock(RateSourceRepository.class);
		SourceFetchRunRepository runs = mock(SourceFetchRunRepository.class);
		OfficialRateRepository officialRates = mock(OfficialRateRepository.class);
		TransactionMocks transactions = transactionMocks();
		OfficialRateIngestionService service = officialService(rateSources, runs, officialRates, transactions);
		UUID runId = UUID.randomUUID();
		UUID sourceId = UUID.randomUUID();
		OfficialRateSource source = new OfficialRateSource(sourceId, "MD", "bnm", "bnm-official-xml", "https://example.test");
		List<OfficialRateUpsert> rates = officialRates();
		DataIntegrityViolationException failure = new DataIntegrityViolationException("bad official rate");

		when(officialRates.upsertRates(sourceId, "MD", rates)).thenThrow(failure);

		assertThatThrownBy(() -> service.saveFetchedRates(runId, source, rates)).isSameAs(failure);

		verify(transactions.manager()).rollback(transactions.status());
		verify(transactions.manager(), never()).commit(any(TransactionStatus.class));
		verify(runs, never()).finish(eq(runId), eq("SUCCESS"), anyInt(), any());
		verify(rateSources, never()).markFetched(sourceId);
	}

	@Test
	void commitsOfficialWritesOnSuccess() {
		RateSourceRepository rateSources = mock(RateSourceRepository.class);
		SourceFetchRunRepository runs = mock(SourceFetchRunRepository.class);
		OfficialRateRepository officialRates = mock(OfficialRateRepository.class);
		TransactionMocks transactions = transactionMocks();
		OfficialRateIngestionService service = officialService(rateSources, runs, officialRates, transactions);
		UUID runId = UUID.randomUUID();
		UUID sourceId = UUID.randomUUID();
		OfficialRateSource source = new OfficialRateSource(sourceId, "MD", "bnm", "bnm-official-xml", "https://example.test");
		List<OfficialRateUpsert> rates = officialRates();

		when(officialRates.upsertRates(sourceId, "MD", rates)).thenReturn(1);

		int upserted = service.saveFetchedRates(runId, source, rates);

		assertThat(upserted).isEqualTo(1);
		verify(runs).finish(runId, "SUCCESS", 1, null);
		verify(rateSources).markFetched(sourceId);
		verify(transactions.manager()).commit(transactions.status());
		verify(transactions.manager(), never()).rollback(any(TransactionStatus.class));
	}

	private TransactionMocks transactionMocks() {
		PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();

		when(manager.getTransaction(any())).thenReturn(status);

		return new TransactionMocks(manager, status);
	}

	private List<OfficialRateUpsert> officialRates() {
		LocalDate rateDate = LocalDate.parse("2026-05-18");

		return List.of(new OfficialRateUpsert("EUR", rateDate, 1, new BigDecimal("19.35")));
	}

	private OfficialRateIngestionService officialService(RateSourceRepository rateSources, SourceFetchRunRepository runs,
			OfficialRateRepository officialRates, TransactionMocks transactions) {
		PlatformTransactionManager manager = transactions.manager();

		return new OfficialRateIngestionService(rateSources, runs, officialRates, manager);
	}

	private record TransactionMocks(PlatformTransactionManager manager, TransactionStatus status) {
	}
}
