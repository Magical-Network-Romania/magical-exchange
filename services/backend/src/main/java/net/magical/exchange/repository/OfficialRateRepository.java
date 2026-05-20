package net.magical.exchange.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import net.magical.exchange.api.ExchangeDtos.OfficialRateHistoryPoint;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OfficialRateRepository {

	private static final String UPSERT_OFFICIAL_RATE_SQL = """
			INSERT INTO official_exchange_rates (country_code, currency_code, rate_date, unit, rate, source_id)
			SELECT :country, :currency, :rateDate, :unit, :rate, :sourceId
			WHERE EXISTS (
				SELECT 1
				FROM currencies
				WHERE code = :currency
			)
			ON CONFLICT (country_code, currency_code, rate_date) DO UPDATE SET
				unit = EXCLUDED.unit,
				rate = EXCLUDED.rate,
				source_id = EXCLUDED.source_id,
				fetched_at = NOW()
			""";

	private final JdbcClient jdbcClient;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public OfficialRateRepository(JdbcClient jdbcClient, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.jdbcClient = jdbcClient;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public LocalDate findLatestDate(String country) {
		String sql = """
				SELECT MAX(rate_date)
				FROM official_exchange_rates
				WHERE country_code = :country
				""";

		List<LocalDate> dates = jdbcClient.sql(sql).param("country", country).query(this::mapNullableLocalDate).list();

		if (dates.isEmpty()) {
			return null;
		}

		return dates.get(0);
	}

	public List<OfficialRateRecord> findByCountryAndDate(String country, LocalDate rateDate) {
		String sql = """
				SELECT official_rate.currency_code,
					official_rate.unit,
					official_rate.rate,
					official_rate.fetched_at,
					official_rate.source_slug
				FROM official_rates_with_source official_rate
				WHERE official_rate.country_code = :country
					AND official_rate.rate_date = :rateDate
				ORDER BY official_rate.currency_code
				""";

		return jdbcClient.sql(sql).param("country", country).param("rateDate", rateDate).query(this::mapOfficialRate).list();
	}

	public List<OfficialRateHistoryPoint> findHistory(String country, String currency, LocalDate from, LocalDate to) {
		String sql = """
				SELECT rate_date,
					unit,
					rate,
					fetched_at
				FROM official_exchange_rates
				WHERE country_code = :country
					AND currency_code = :currency
					AND rate_date BETWEEN :from AND :to
				ORDER BY rate_date
				""";

		return jdbcClient.sql(sql).param("country", country).param("currency", currency).param("from", from).param("to", to)
				.query(this::mapOfficialRateHistoryPoint).list();
	}

	public int upsertRates(UUID sourceId, String country, List<OfficialRateUpsert> rates) {
		if (rates.isEmpty()) {
			return 0;
		}

		SqlParameterSource[] parameters = officialRateParameters(sourceId, country, rates);
		return sumRows(namedParameterJdbcTemplate.batchUpdate(UPSERT_OFFICIAL_RATE_SQL, parameters));
	}

	private SqlParameterSource[] officialRateParameters(UUID sourceId, String country, List<OfficialRateUpsert> rates) {
		return rates.stream().map(rate -> officialRateParameters(sourceId, country, rate)).toArray(SqlParameterSource[]::new);
	}

	private SqlParameterSource officialRateParameters(UUID sourceId, String country, OfficialRateUpsert rate) {
		return new MapSqlParameterSource().addValue("country", country).addValue("currency", rate.currencyCode())
				.addValue("rateDate", rate.rateDate()).addValue("unit", rate.unit()).addValue("rate", rate.rate())
				.addValue("sourceId", sourceId);
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

	private OfficialRateRecord mapOfficialRate(ResultSet resultSet, int rowNumber) throws SQLException {
		String currencyCode = resultSet.getString("currency_code");
		String sourceSlug = resultSet.getString("source_slug");
		OffsetDateTime fetchedAt = resultSet.getObject("fetched_at", OffsetDateTime.class);
		int unit = resultSet.getInt("unit");
		BigDecimal rate = resultSet.getBigDecimal("rate");

		return new OfficialRateRecord(currencyCode, unit, rate, sourceSlug, fetchedAt);
	}

	private OfficialRateHistoryPoint mapOfficialRateHistoryPoint(ResultSet resultSet, int rowNumber) throws SQLException {
		return new OfficialRateHistoryPoint(resultSet.getObject("rate_date", LocalDate.class), resultSet.getInt("unit"),
				resultSet.getBigDecimal("rate"), resultSet.getObject("fetched_at", OffsetDateTime.class));
	}

	private LocalDate mapNullableLocalDate(ResultSet resultSet, int rowNumber) throws SQLException {
		return resultSet.getObject(1, LocalDate.class);
	}

	public record OfficialRateRecord(String currencyCode, int unit, BigDecimal rate, String sourceSlug, OffsetDateTime fetchedAt) {
	}

	public record OfficialRateUpsert(String currencyCode, LocalDate rateDate, int unit, BigDecimal rate) {
	}
}
