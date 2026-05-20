package net.magical.exchange.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RateSourceRepository {

	private final JdbcClient jdbcClient;

	public RateSourceRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<OfficialRateSource> findDueOfficialSources() {
		String sql = """
				SELECT id,
					country_code,
					slug,
					parser_key,
					source_url
				FROM due_rate_sources
				WHERE source_type = 'OFFICIAL_XML'
				ORDER BY slug
				""";

		return jdbcClient.sql(sql).query(this::mapOfficialSource).list();
	}

	public List<MarketRateSource> findDueMarketSources() {
		String sql = """
				SELECT id,
					institution_id,
					location_id,
					country_code,
					city_slug,
					slug,
					source_url,
					rate_type
				FROM due_rate_sources
				WHERE source_type = 'MARKET_HTML'
				ORDER BY slug
				""";

		return jdbcClient.sql(sql).query(this::mapMarketSource).list();
	}

	public void markFetched(UUID sourceId) {
		String sql = """
				UPDATE rate_sources
				SET last_fetched_at = :lastFetchedAt,
					updated_at = NOW()
				WHERE id = :sourceId
				""";

		jdbcClient.sql(sql).param("sourceId", sourceId).param("lastFetchedAt", OffsetDateTime.now()).update();
	}

	private OfficialRateSource mapOfficialSource(ResultSet resultSet, int rowNumber) throws SQLException {
		UUID id = resultSet.getObject("id", UUID.class);
		String countryCode = resultSet.getString("country_code");
		String slug = resultSet.getString("slug");
		String parserKey = resultSet.getString("parser_key");
		String sourceUrl = resultSet.getString("source_url");

		return new OfficialRateSource(id, countryCode, slug, parserKey, sourceUrl);
	}

	private MarketRateSource mapMarketSource(ResultSet resultSet, int rowNumber) throws SQLException {
		UUID id = resultSet.getObject("id", UUID.class);
		UUID institutionId = resultSet.getObject("institution_id", UUID.class);
		UUID locationId = resultSet.getObject("location_id", UUID.class);
		String countryCode = resultSet.getString("country_code");
		String citySlug = resultSet.getString("city_slug");
		String slug = resultSet.getString("slug");
		String sourceUrl = resultSet.getString("source_url");
		String rateType = resultSet.getString("rate_type");

		return new MarketRateSource(id, institutionId, locationId, countryCode, citySlug, slug, sourceUrl, rateType);
	}

	public record OfficialRateSource(UUID id, String countryCode, String slug, String parserKey, String sourceUrl) {
	}

	public record MarketRateSource(UUID id, UUID institutionId, UUID locationId, String countryCode, String citySlug, String slug,
			String sourceUrl, String rateType) {
	}
}
