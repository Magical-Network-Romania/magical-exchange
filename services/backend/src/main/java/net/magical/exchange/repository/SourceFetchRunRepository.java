package net.magical.exchange.repository;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SourceFetchRunRepository {

	private final JdbcClient jdbcClient;

	public SourceFetchRunRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public UUID start(UUID sourceId) {
		String sql = """
				INSERT INTO source_fetch_runs (source_id, status)
				VALUES (:sourceId, 'STARTED')
				RETURNING id
				""";

		return jdbcClient.sql(sql).param("sourceId", sourceId).query(UUID.class).single();
	}

	public void finish(UUID runId, String status, int itemsUpserted, String errorMessage) {
		String sql = """
				UPDATE source_fetch_runs
				SET status = :status,
					finished_at = NOW(),
					items_upserted = :itemsUpserted,
					error_message = :errorMessage
				WHERE id = :runId
				""";

		jdbcClient.sql(sql).param("runId", runId).param("status", status).param("itemsUpserted", itemsUpserted)
				.param("errorMessage", errorMessage).update();
	}
}
