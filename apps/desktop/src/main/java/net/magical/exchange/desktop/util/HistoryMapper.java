package net.magical.exchange.desktop.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import net.magical.exchange.desktop.model.OfficialRateHistoryPoint;
import net.magical.exchange.desktop.model.UiLocale;

public final class HistoryMapper {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private HistoryMapper() {
	}

	public static List<HistoryChartPoint> toHistoryChartData(List<OfficialRateHistoryPoint> history, UiLocale locale) {
		return history.stream().map(point -> toHistoryChartPoint(point, locale)).toList();
	}

	private static HistoryChartPoint toHistoryChartPoint(OfficialRateHistoryPoint point, UiLocale locale) {
		BigDecimal unitRate = point.rate().divide(BigDecimal.valueOf(point.unit()), MATH_CONTEXT);

		return new HistoryChartPoint(point.rateDate(), ExchangeFormat.formatDate(point.rateDate(), locale), unitRate);
	}
}
