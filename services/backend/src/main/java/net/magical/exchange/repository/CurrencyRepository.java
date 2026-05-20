package net.magical.exchange.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CurrencyRepository {

	private final JdbcClient jdbcClient;

	public CurrencyRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<CurrencyRecord> findAllActive() {
		String sql = """
				SELECT currency.code,
					currency.numeric_code,
					currency.minor_units
				FROM active_currencies currency
				ORDER BY currency.code
				""";

		return jdbcClient.sql(sql).query(this::mapCurrency).list();
	}

	public List<CurrencyRecord> findByCodes(Collection<String> codes) {
		if (codes.isEmpty()) {
			return List.of();
		}

		String sql = """
				SELECT currency.code,
					currency.numeric_code,
					currency.minor_units
				FROM currencies currency
				WHERE currency.code IN (:codes)
				ORDER BY currency.code
				""";

		return jdbcClient.sql(sql).param("codes", codes).query(this::mapCurrency).list();
	}

	public List<String> findActiveCodes() {
		String sql = """
				SELECT code
				FROM active_currencies
				ORDER BY code
				""";

		return jdbcClient.sql(sql).query(String.class).list();
	}

	private CurrencyRecord mapCurrency(ResultSet resultSet, int rowNumber) throws SQLException {
		String code = resultSet.getString("code");
		String numericCode = resultSet.getString("numeric_code");
		int minorUnits = resultSet.getInt("minor_units");

		return new CurrencyRecord(code, numericCode, minorUnits);
	}

	public record CurrencyRecord(String code, String numericCode, int minorUnits) {
	}
}
