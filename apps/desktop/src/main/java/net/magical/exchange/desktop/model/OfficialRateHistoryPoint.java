package net.magical.exchange.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OfficialRateHistoryPoint(LocalDate rateDate, int unit, BigDecimal rate, OffsetDateTime fetchedAt) {
}
