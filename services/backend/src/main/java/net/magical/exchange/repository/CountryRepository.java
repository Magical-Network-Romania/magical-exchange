package net.magical.exchange.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CountryRepository {

	private final JdbcClient jdbcClient;

	public CountryRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<CountryRecord> findAllActive() {
		String sql = """
				SELECT country.code,
					country.base_currency_code,
					country.default_locale_code
				FROM active_countries country
				ORDER BY country.code
				""";

		return jdbcClient.sql(sql).query(this::mapCountry).list();
	}

	public Optional<CountryRecord> findActiveByCode(String country) {
		String sql = """
				SELECT country.code,
					country.base_currency_code,
					country.default_locale_code
				FROM active_countries country
				WHERE country.code = :country
				""";

		List<CountryRecord> countries = jdbcClient.sql(sql).param("country", country).query(this::mapCountry).list();

		return countries.stream().findFirst();
	}

	private CountryRecord mapCountry(ResultSet resultSet, int rowNumber) throws SQLException {
		String code = resultSet.getString("code");
		String baseCurrencyCode = resultSet.getString("base_currency_code");
		String defaultLocaleCode = resultSet.getString("default_locale_code");

		return new CountryRecord(code, baseCurrencyCode, defaultLocaleCode);
	}

	public record CountryRecord(String code, String baseCurrencyCode, String defaultLocaleCode) {
	}
}
