package net.magical.exchange.ingestion;

import net.magical.exchange.ingestion.exception.MarketRateIngestionException;
import net.magical.exchange.ingestion.exception.OfficialRateIngestionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SourceFetchScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SourceFetchScheduler.class);

	private final boolean enabled;
	private final MarketRateIngestionService marketRateIngestionService;
	private final OfficialRateIngestionService officialRateIngestionService;
	private final boolean startupEnabled;

	public SourceFetchScheduler(@Value("${magical.exchange.ingestion.enabled:true}") boolean enabled,
			@Value("${magical.exchange.ingestion.startup-enabled:true}") boolean startupEnabled,
			OfficialRateIngestionService officialRateIngestionService, MarketRateIngestionService marketRateIngestionService) {
		this.enabled = enabled;
		this.startupEnabled = startupEnabled;
		this.officialRateIngestionService = officialRateIngestionService;
		this.marketRateIngestionService = marketRateIngestionService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void fetchOnStartup() {
		if (enabled && startupEnabled) {
			fetchRates();
		}
	}

	@Scheduled(fixedDelayString = "${magical.exchange.ingestion.fixed-delay-ms:3600000}")
	public void fetchRates() {
		if (!enabled) {
			return;
		}

		try {
			officialRateIngestionService.fetchDueOfficialRates();
		} catch (OfficialRateIngestionException exception) {
			LOGGER.warn("Official rate ingestion failed: {}", exception.getMessage());
		} catch (DataAccessException exception) {
			LOGGER.warn("Official rate ingestion database failure: {}", exception.getMessage());
		}

		try {
			marketRateIngestionService.fetchDueMarketRates();
		} catch (MarketRateIngestionException exception) {
			LOGGER.warn("Market rate ingestion failed: {}", exception.getMessage());
		} catch (DataAccessException exception) {
			LOGGER.warn("Market rate ingestion database failure: {}", exception.getMessage());
		}
	}
}
