package net.magical.exchange.api;

import java.util.Locale;
import net.magical.exchange.service.exception.BadRequestException;

public enum BestRateOperation {

	BUY_FOREIGN_CURRENCY, SELL_FOREIGN_CURRENCY;

	public static BestRateOperation parse(String value) {
		if (value == null || value.isBlank()) {
			throw new BadRequestException("operation is required");
		}

		String normalizedValue = value.trim().toUpperCase(Locale.ROOT);

		for (BestRateOperation operation : values()) {
			if (operation.name().equals(normalizedValue)) {
				return operation;
			}
		}

		throw new BadRequestException("Unsupported operation: " + value);
	}
}
