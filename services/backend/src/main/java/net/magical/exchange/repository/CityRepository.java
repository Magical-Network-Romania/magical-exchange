package net.magical.exchange.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CityRepository {

	private final JdbcClient jdbcClient;

	public CityRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<CityRecord> findActiveByCountry(String country) {
		String sql = """
				SELECT city.country_code,
					city.slug,
					city.timezone
				FROM active_cities city
				WHERE city.country_code = :country
				ORDER BY city.slug
				""";

		return jdbcClient.sql(sql).param("country", country).query(this::mapCity).list();
	}

	public Optional<CityRecord> findActiveByCountryAndSlug(String country, String city) {
		String sql = """
				SELECT city.country_code,
					city.slug,
					city.timezone
				FROM active_cities city
				WHERE city.country_code = :country
					AND city.slug = :city
				""";

		List<CityRecord> cities = jdbcClient.sql(sql).param("country", country).param("city", city).query(this::mapCity).list();

		return cities.stream().findFirst();
	}

	private CityRecord mapCity(ResultSet resultSet, int rowNumber) throws SQLException {
		String countryCode = resultSet.getString("country_code");
		String slug = resultSet.getString("slug");
		String timezone = resultSet.getString("timezone");

		return new CityRecord(countryCode, slug, timezone);
	}

	public record CityRecord(String countryCode, String slug, String timezone) {
	}
}
