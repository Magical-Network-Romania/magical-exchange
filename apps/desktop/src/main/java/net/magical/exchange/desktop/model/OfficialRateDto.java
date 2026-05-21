package net.magical.exchange.desktop.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OfficialRateDto(CurrencyDto currency, int unit, BigDecimal rate, String source, OffsetDateTime fetchedAt) {
}
