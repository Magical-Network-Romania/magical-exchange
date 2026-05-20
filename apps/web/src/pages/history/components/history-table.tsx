import type { Translate } from "@/app-types";
import { EmptyState } from "@/components/ui/empty-state";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDate, formatDateTime, formatRate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { OfficialRateHistoryPoint } from "@/services/exchange-api";

type HistoryTableProps = {
	emptyLabel: string;
	history: OfficialRateHistoryPoint[];
	locale: UiLocale;
	t: Translate;
};

export function HistoryTable({ emptyLabel, history, locale, t }: HistoryTableProps) {
	if (history.length === 0) {
		return <EmptyState>{emptyLabel}</EmptyState>;
	}

	return (
		<Table>
			<TableHeader>
				<TableRow>
					<TableHead>{t("from")}</TableHead>
					<TableHead>{t("unit")}</TableHead>
					<TableHead>{t("rate")}</TableHead>
					<TableHead>{t("fetched")}</TableHead>
				</TableRow>
			</TableHeader>
			<TableBody>
				{[...history].reverse().map((point) => (
					<TableRow key={`${point.rateDate}-${point.rate}`}>
						<TableCell>{formatDate(point.rateDate, locale)}</TableCell>
						<TableCell>{point.unit}</TableCell>
						<TableCell className="font-semibold">{formatRate(point.rate, locale)}</TableCell>
						<TableCell>{formatDateTime(point.fetchedAt, locale)}</TableCell>
					</TableRow>
				))}
			</TableBody>
		</Table>
	);
}
