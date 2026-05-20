package net.magical.exchange.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import net.magical.exchange.repository.LocationRepository.InstitutionRecord;
import net.magical.exchange.repository.LocationRepository.LocationRecord;
import net.magical.exchange.repository.RateSourceRepository.MarketRateSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MarketRateRepository {

	private static final String INSERT_MARKET_RATE_SQL = """
			INSERT INTO market_exchange_rates (
				batch_id,
				currency_code,
				rate_type,
				buy_rate,
				sell_rate,
				official_rate,
				unit
			)
			VALUES (:batchId, :currency, :rateType, :buyRate, :sellRate, :officialRate, :unit)
			""";

	private final JdbcClient jdbcClient;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public MarketRateRepository(JdbcClient jdbcClient, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.jdbcClient = jdbcClient;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<MarketRateRecord> findLatestByCity(String country, String city, String currency, String rateType) {
		String currencyFilter = currency == null ? "" : "AND latest.currency_code = :currency\n";
		String sql = """
				SELECT latest.*
				FROM latest_market_rates_by_location latest
				WHERE latest.country_code = :country
					AND latest.city_slug = :city
					""" + currencyFilter + """
				AND latest.rate_type = :rateType
				ORDER BY latest.institution_name, latest.location_name, latest.currency_code
				""";

		var statement = jdbcClient.sql(sql).param("country", country).param("city", city).param("rateType", rateType);

		if (currency != null) {
			statement = statement.param("currency", currency);
		}

		return statement.query(this::mapMarketRate).list();
	}

	public UUID insertBatch(MarketRateSource source, String checksum) {
		String sql = """
				INSERT INTO market_rate_batches (
					source_id,
					institution_id,
					location_id,
					country_code,
					city_slug,
					checksum
				)
				VALUES (:sourceId, :institutionId, :locationId, :country, :city, :checksum)
				ON CONFLICT (source_id, checksum) DO NOTHING
				RETURNING id
				""";

		var statement = jdbcClient.sql(sql).param("sourceId", source.id()).param("institutionId", source.institutionId());
		statement = statement.param("locationId", source.locationId()).param("country", source.countryCode());
		statement = statement.param("city", source.citySlug()).param("checksum", checksum);
		List<UUID> batchIds = statement.query(UUID.class).list();

		if (batchIds.isEmpty()) {
			return null;
		}

		return batchIds.get(0);
	}

	public int insertRates(UUID batchId, List<MarketRateUpsert> rates) {
		if (rates.isEmpty()) {
			return 0;
		}

		return sumRows(namedParameterJdbcTemplate.batchUpdate(INSERT_MARKET_RATE_SQL, marketRateParameters(batchId, rates)));
	}

	private SqlParameterSource[] marketRateParameters(UUID batchId, List<MarketRateUpsert> rates) {
		return rates.stream().map(rate -> marketRateParameters(batchId, rate)).toArray(SqlParameterSource[]::new);
	}

	private SqlParameterSource marketRateParameters(UUID batchId, MarketRateUpsert rate) {
		return new MapSqlParameterSource().addValue("batchId", batchId).addValue("currency", rate.currency())
				.addValue("rateType", rate.type()).addValue("buyRate", rate.buy()).addValue("sellRate", rate.sell())
				.addValue("officialRate", rate.official()).addValue("unit", rate.unit());
	}

	private int sumRows(int[] rowCounts) {
		int rows = 0;

		for (int rowCount : rowCounts) {
			if (rowCount == Statement.SUCCESS_NO_INFO) {
				rows++;
			} else if (rowCount > 0) {
				rows += rowCount;
			}
		}

		return rows;
	}

	private MarketRateRecord mapMarketRate(ResultSet resultSet, int rowNumber) throws SQLException {
		String currency = resultSet.getString("currency_code");
		InstitutionRecord provider = mapInstitution(resultSet);
		LocationRecord office = mapLocation(resultSet, provider);
		String rateType = resultSet.getString("rate_type");
		BigDecimal buyRate = resultSet.getBigDecimal("buy_rate");
		BigDecimal sellRate = resultSet.getBigDecimal("sell_rate");
		BigDecimal official = resultSet.getBigDecimal("official_rate");
		int unit = resultSet.getInt("unit");
		OffsetDateTime fetched = resultSet.getObject("fetched_at", OffsetDateTime.class);
		OffsetDateTime published = resultSet.getObject("source_published_at", OffsetDateTime.class);

		return new MarketRateRecord(currency, rateType, buyRate, sellRate, official, unit, fetched, published, provider, office);
	}

	private InstitutionRecord mapInstitution(ResultSet resultSet) throws SQLException {
		UUID id = resultSet.getObject("institution_id", UUID.class);
		String slug = resultSet.getString("institution_slug");
		String name = resultSet.getString("institution_name");
		String type = resultSet.getString("institution_type");
		String websiteUrl = resultSet.getString("website_url");

		return new InstitutionRecord(id, slug, name, type, websiteUrl);
	}

	private LocationRecord mapLocation(ResultSet resultSet, InstitutionRecord institution) throws SQLException {
		UUID id = resultSet.getObject("location_id", UUID.class);
		String country = resultSet.getString("country_code");
		String city = resultSet.getString("city_slug");
		String slug = resultSet.getString("location_slug");
		String name = resultSet.getString("location_name");
		String address = resultSet.getString("address");
		BigDecimal latitude = resultSet.getBigDecimal("latitude");
		BigDecimal longitude = resultSet.getBigDecimal("longitude");
		String phone = resultSet.getString("phone");
		String email = resultSet.getString("email");

		return new LocationRecord(id, country, city, slug, name, address, latitude, longitude, phone, email, institution);
	}

	public record MarketRateRecord(String currencyCode, String type, BigDecimal buy, BigDecimal sell, BigDecimal official, int unit,
			OffsetDateTime fetchedAt, OffsetDateTime publishedAt, InstitutionRecord institution, LocationRecord location) {
	}

	public record MarketRateUpsert(String currency, String type, BigDecimal buy, BigDecimal sell, BigDecimal official, int unit) {
	}
}
