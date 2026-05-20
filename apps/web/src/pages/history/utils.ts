import { formatDate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { OfficialRateHistoryPoint } from "@/services/exchange-api";

export type HistoryChartPoint = {
	date: string;
	formattedDate: string;
	rate: number;
};

export function toHistoryChartData(history: OfficialRateHistoryPoint[], locale: UiLocale) {
	return history.map((point) => ({
		date: point.rateDate,
		formattedDate: formatDate(point.rateDate, locale),
		rate: point.rate / point.unit
	}));
}
