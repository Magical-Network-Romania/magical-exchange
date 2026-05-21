package net.magical.exchange.desktop.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoryChartPoint(LocalDate date, String formattedDate, BigDecimal rate) {
}
