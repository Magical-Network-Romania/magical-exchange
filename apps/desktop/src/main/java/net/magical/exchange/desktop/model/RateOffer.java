package net.magical.exchange.desktop.model;

import java.math.BigDecimal;

public record RateOffer(MarketRateDto rate, BigDecimal value) {
}
