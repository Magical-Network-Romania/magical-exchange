package net.magical.exchange.ingestion.exception;

public class OfficialRateIngestionException extends RuntimeException {

	public OfficialRateIngestionException(String message) {
		super(message);
	}

	public OfficialRateIngestionException(String message, Throwable cause) {
		super(message, cause);
	}
}
