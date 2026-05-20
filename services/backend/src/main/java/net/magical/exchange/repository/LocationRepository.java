package net.magical.exchange.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LocationRepository {

	private final JdbcClient jdbcClient;

	public LocationRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<LocationRecord> findByCountryAndCity(String country, String city) {
		String sql = """
				SELECT location.location_id,
					location.country_code,
					location.city_slug,
					location.location_slug,
					location.location_name,
					location.address,
					location.latitude,
					location.longitude,
					location.phone,
					location.email,
					location.institution_id,
					location.institution_slug,
					location.institution_name,
					location.institution_type,
					location.website_url
				FROM active_locations_with_institution location
				WHERE location.country_code = :country
					AND location.city_slug = :city
				ORDER BY location.institution_name, location.location_name
				""";

		return jdbcClient.sql(sql).param("country", country).param("city", city).query(this::mapLocation).list();
	}

	private LocationRecord mapLocation(ResultSet resultSet, int rowNumber) throws SQLException {
		InstitutionRecord institution = mapInstitution(resultSet);
		UUID id = resultSet.getObject("location_id", UUID.class);
		String countryCode = resultSet.getString("country_code");
		String citySlug = resultSet.getString("city_slug");
		String slug = resultSet.getString("location_slug");
		String name = resultSet.getString("location_name");
		String address = resultSet.getString("address");
		BigDecimal latitude = resultSet.getBigDecimal("latitude");
		BigDecimal longitude = resultSet.getBigDecimal("longitude");
		String phone = resultSet.getString("phone");
		String email = resultSet.getString("email");

		return new LocationRecord(id, countryCode, citySlug, slug, name, address, latitude, longitude, phone, email, institution);
	}

	private InstitutionRecord mapInstitution(ResultSet resultSet) throws SQLException {
		UUID id = resultSet.getObject("institution_id", UUID.class);
		String slug = resultSet.getString("institution_slug");
		String name = resultSet.getString("institution_name");
		String type = resultSet.getString("institution_type");
		String websiteUrl = resultSet.getString("website_url");

		return new InstitutionRecord(id, slug, name, type, websiteUrl);
	}

	public record InstitutionRecord(UUID id, String slug, String name, String type, String websiteUrl) {
	}

	public record LocationRecord(UUID id, String countryCode, String citySlug, String slug, String name, String address,
			BigDecimal latitude, BigDecimal longitude, String phone, String email, InstitutionRecord institution) {
	}
}
