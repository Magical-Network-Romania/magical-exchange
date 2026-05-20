package net.magical.exchange.ingestion.exception;

public class MarketRateIngestionException extends RuntimeException {

	public MarketRateIngestionException(String message) {
		super(message);
	}

	public MarketRateIngestionException(String message, Throwable cause) {
		super(message, cause);
	}
}
