package net.magical.exchange.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LocalizationRepository {

	private final JdbcClient jdbcClient;

	public LocalizationRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public boolean existsActive(String locale) {
		String sql = """
				SELECT EXISTS (
					SELECT 1
					FROM locales
					WHERE code = :locale
						AND active
				)
				""";

		Boolean exists = jdbcClient.sql(sql).param("locale", locale).query(Boolean.class).single();

		return Boolean.TRUE.equals(exists);
	}

	public Map<String, String> findCountryNames(Collection<String> countryCodes, String locale) {
		if (countryCodes.isEmpty()) {
			return Map.of();

		}

		String sql = """
				SELECT country.code AS code,
					COALESCE(translation.name, fallback_translation.name, country.code) AS name
				FROM countries country
				LEFT JOIN country_translations translation
					ON translation.country_code = country.code
					AND translation.locale_code = :locale
				LEFT JOIN country_translations fallback_translation
					ON fallback_translation.country_code = country.code
					AND fallback_translation.locale_code = country.default_locale_code
				WHERE country.code IN (:countryCodes)
				""";

		List<LocalizedName> names = jdbcClient.sql(sql).param("countryCodes", countryCodes).param("locale", locale)
				.query(this::mapLocalizedName).list();

		return toNameMap(names);
	}

	public Map<String, String> findCityNames(String country, Collection<String> citySlugs, String locale) {
		if (citySlugs.isEmpty()) {
			return Map.of();
		}

		String sql = """
				SELECT city.slug AS code,
					COALESCE(translation.name, fallback_translation.name, city.slug) AS name
				FROM cities city
				JOIN countries country ON country.code = city.country_code
				LEFT JOIN city_translations translation
					ON translation.country_code = city.country_code
					AND translation.city_slug = city.slug
					AND translation.locale_code = :locale
				LEFT JOIN city_translations fallback_translation
					ON fallback_translation.country_code = city.country_code
					AND fallback_translation.city_slug = city.slug
					AND fallback_translation.locale_code = country.default_locale_code
				WHERE city.country_code = :country
					AND city.slug IN (:citySlugs)
				""";

		List<LocalizedName> names = jdbcClient.sql(sql).param("country", country).param("citySlugs", citySlugs)
				.param("locale", locale).query(this::mapLocalizedName).list();

		return toNameMap(names);
	}

	public Map<String, String> findCurrencyNames(Collection<String> currencyCodes, String locale) {
		if (currencyCodes.isEmpty()) {
			return Map.of();

		}

		String sql = """
				SELECT currency.code AS code,
					COALESCE(translation.name, fallback_translation.name, currency.code) AS name
				FROM currencies currency
				LEFT JOIN currency_translations translation
					ON translation.currency_code = currency.code
					AND translation.locale_code = :locale
				LEFT JOIN currency_translations fallback_translation
					ON fallback_translation.currency_code = currency.code
					AND fallback_translation.locale_code = 'en'
				WHERE currency.code IN (:currencyCodes)
				""";

		List<LocalizedName> names = jdbcClient.sql(sql).param("currencyCodes", currencyCodes).param("locale", locale)
				.query(this::mapLocalizedName).list();

		return toNameMap(names);
	}

	private LocalizedName mapLocalizedName(ResultSet resultSet, int rowNumber) throws SQLException {
		return new LocalizedName(resultSet.getString("code"), resultSet.getString("name"));
	}

	private Map<String, String> toNameMap(List<LocalizedName> names) {
		return names.stream().collect(Collectors.toMap(LocalizedName::code, LocalizedName::name));
	}

	private record LocalizedName(String code, String name) {
	}
}
