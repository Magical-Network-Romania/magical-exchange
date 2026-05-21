package net.magical.exchange.desktop.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MarketRateDto(CurrencyDto currency, String rateType, BigDecimal buyRate, BigDecimal sellRate, BigDecimal officialRate,
		int unit, OffsetDateTime fetchedAt, OffsetDateTime publishedAt, InstitutionDto institution, LocationDto location) {
}
