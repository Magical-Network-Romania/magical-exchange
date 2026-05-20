package net.magical.exchange.ingestion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.magical.exchange.ingestion.exception.OfficialRateIngestionException;
import net.magical.exchange.repository.OfficialRateRepository;
import net.magical.exchange.repository.OfficialRateRepository.OfficialRateUpsert;
import net.magical.exchange.repository.RateSourceRepository;
import net.magical.exchange.repository.RateSourceRepository.OfficialRateSource;
import net.magical.exchange.repository.SourceFetchRunRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class OfficialRateIngestionService {

	private static final DateTimeFormatter BNM_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
	private static final ZoneId MOLDOVA_ZONE = ZoneId.of("Europe/Chisinau");
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

	private final HttpClient httpClient;
	private final OfficialRateRepository officialRateRepository;
	private final RateSourceRepository rateSourceRepository;
	private final SourceFetchRunRepository sourceFetchRunRepository;
	private final TransactionTemplate transactionTemplate;

	public OfficialRateIngestionService(RateSourceRepository rateSourceRepository, SourceFetchRunRepository sourceFetchRunRepository,
			OfficialRateRepository officialRateRepository, PlatformTransactionManager transactionManager) {
		this.rateSourceRepository = rateSourceRepository;
		this.sourceFetchRunRepository = sourceFetchRunRepository;
		this.officialRateRepository = officialRateRepository;
		this.httpClient = buildHttpClient();
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public void fetchDueOfficialRates() {
		List<OfficialRateSource> sources = rateSourceRepository.findDueOfficialSources();
		for (OfficialRateSource source : sources) {
			fetchSource(source);
		}
	}

	private HttpClient buildHttpClient() {
		HttpClient.Builder clientBuilder = HttpClient.newBuilder();

		clientBuilder.connectTimeout(CONNECT_TIMEOUT);
		clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);

		return clientBuilder.build();
	}

	private void fetchSource(OfficialRateSource source) {
		UUID runId = sourceFetchRunRepository.start(source.id());

		if (!"bnm-official-xml".equals(source.parserKey())) {
			sourceFetchRunRepository.finish(runId, "SKIPPED", 0, "Unsupported official parser: " + source.parserKey());
			return;
		}

		try {
			List<OfficialRateUpsert> rates = fetchBnmRates(source);
			saveFetchedRates(runId, source, rates);
		} catch (OfficialRateIngestionException exception) {
			sourceFetchRunRepository.finish(runId, "FAILED", 0, exception.getMessage());
		} catch (DataAccessException exception) {
			sourceFetchRunRepository.finish(runId, "FAILED", 0, databaseFailureMessage(source.slug(), exception));
		}
	}

	int saveFetchedRates(UUID runId, OfficialRateSource source, List<OfficialRateUpsert> rates) {
		Integer itemsUpserted = transactionTemplate.execute(status -> {
			int upserted = officialRateRepository.upsertRates(source.id(), source.countryCode(), rates);

			sourceFetchRunRepository.finish(runId, "SUCCESS", upserted, null);
			rateSourceRepository.markFetched(source.id());

			return upserted;
		});

		if (itemsUpserted == null) {
			return 0;
		}

		return itemsUpserted;
	}

	private String databaseFailureMessage(String sourceSlug, DataAccessException exception) {
		return "Official rate ingestion database failure for " + sourceSlug + ": " + exception.getMessage();
	}

	private List<OfficialRateUpsert> fetchBnmRates(OfficialRateSource source) {
		LocalDate date = LocalDate.now(MOLDOVA_ZONE);
		String url = sourceUrl(source).replace("{date}", BNM_DATE_FORMATTER.format(date));
		HttpRequest request = HttpRequest.newBuilder(sourceUri(url, source.slug())).timeout(REQUEST_TIMEOUT).GET().build();

		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				String message = "Official source returned HTTP " + response.statusCode() + ": " + source.slug();

				throw new OfficialRateIngestionException(message);
			}

			return parseBnmXml(response.body(), date);
		} catch (IOException exception) {
			throw new OfficialRateIngestionException("Could not fetch official source: " + source.slug(), exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new OfficialRateIngestionException("Official source fetch was interrupted: " + source.slug(), exception);
		}
	}

	private String sourceUrl(OfficialRateSource source) {
		if (source.sourceUrl() == null || source.sourceUrl().isBlank()) {
			throw new OfficialRateIngestionException("Official source URL is missing: " + source.slug());
		}

		return source.sourceUrl();
	}

	private URI sourceUri(String url, String sourceSlug) {
		try {
			return URI.create(url);
		} catch (IllegalArgumentException exception) {
			throw new OfficialRateIngestionException("Official source URL is invalid: " + sourceSlug, exception);
		}
	}

	private List<OfficialRateUpsert> parseBnmXml(byte[] body, LocalDate fallbackDate) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

			Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(body));
			Element root = document.getDocumentElement();
			LocalDate rateDate = parseBnmDate(root.getAttribute("Date"), fallbackDate);
			NodeList nodes = document.getElementsByTagName("Valute");
			List<OfficialRateUpsert> rates = new ArrayList<>();

			for (int index = 0; index < nodes.getLength(); index++) {
				Node node = nodes.item(index);

				if (node instanceof Element element) {
					rates.add(parseBnmRateElement(element, rateDate));
				}
			}

			return rates;
		} catch (ParserConfigurationException | SAXException | IOException exception) {
			throw bnmParseException(body, exception);
		} catch (DateTimeParseException | NumberFormatException exception) {
			throw bnmParseException(body, exception);
		}
	}

	private OfficialRateIngestionException bnmParseException(byte[] body, Exception exception) {
		String preview = new String(body, 0, Math.min(body.length, 160), StandardCharsets.UTF_8);

		return new OfficialRateIngestionException("Could not parse BNM XML: " + preview, exception);
	}

	private OfficialRateUpsert parseBnmRateElement(Element element, LocalDate rateDate) {
		String currencyCode = childText(element, "CharCode");
		int unit = Integer.parseInt(childText(element, "Nominal"));
		BigDecimal rate = parseDecimal(childText(element, "Value"));

		return new OfficialRateUpsert(currencyCode, rateDate, unit, rate);
	}

	private LocalDate parseBnmDate(String value, LocalDate fallbackDate) {
		if (value == null || value.isBlank()) {
			return fallbackDate;
		}

		return LocalDate.parse(value, BNM_DATE_FORMATTER);
	}

	private BigDecimal parseDecimal(String value) {
		return new BigDecimal(value.trim().replace(',', '.'));
	}

	private String childText(Element element, String tagName) {
		NodeList nodes = element.getElementsByTagName(tagName);

		if (nodes.getLength() == 0) {
			throw new OfficialRateIngestionException("BNM XML is missing " + tagName);
		}

		return nodes.item(0).getTextContent().trim();
	}

}
