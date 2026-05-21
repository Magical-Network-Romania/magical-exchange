import { type ReactNode, useMemo, useState } from "react";
import type { Translate } from "@/app-types";
import { ExportMenuButton } from "@/components/export-menu-button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { InputField, SelectField } from "@/components/ui/field";
import { Skeleton } from "@/components/ui/skeleton";
import { type ExportFormat, exportOfficialRateHistory } from "@/export-data";
import { daysAgoIsoDate, formatDate, todayIsoDate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { BootstrapDto } from "@/services/exchange-api";
import { getForeignCurrencyOptions } from "@/utils/currency-options";
import { HistoryChart } from "./components/history-chart";
import { HistoryTable } from "./components/history-table";
import { useOfficialRateHistory } from "./hooks/use-official-rate-history";
import { toHistoryChartData } from "./utils";

type HistoryPageProps = {
	bootstrap: BootstrapDto | null;
	country: string;
	locale: UiLocale;
	onSelectedCurrencyChange: (currency: string) => void;
	selectedCurrency: string;
	t: Translate;
};

export function HistoryPage({ bootstrap, country, locale, onSelectedCurrencyChange, selectedCurrency, t }: HistoryPageProps) {
	const [from, setFrom] = useState(daysAgoIsoDate(30));
	const [to, setTo] = useState(todayIsoDate());
	const currencyOptions = useMemo(() => getForeignCurrencyOptions(bootstrap, selectedCurrency), [bootstrap, selectedCurrency]);
	const dateRangeError = from > to ? t("invalidDateRange") : null;
	const { error, history, isLoading } = useOfficialRateHistory(country, selectedCurrency, from, to, !dateRangeError);
	const chartData = useMemo(() => toHistoryChartData(history, locale), [history, locale]);
	const baseCurrency = bootstrap?.country.baseCurrencyCode ?? "MDL";
	const countryName = bootstrap?.country.name ?? country;
	let chartContent: ReactNode;

	function handleExport(format: ExportFormat) {
		exportOfficialRateHistory({
			baseCurrency,
			countryCode: country,
			countryName,
			currency: selectedCurrency,
			format,
			from,
			history,
			to
		});
	}

	if (dateRangeError) {
		chartContent = <EmptyState>{dateRangeError}</EmptyState>;
	} else if (isLoading) {
		chartContent = <Skeleton className="h-72 sm:h-80" />;
	} else if (error) {
		chartContent = <EmptyState>{error}</EmptyState>;
	} else if (history.length === 0) {
		chartContent = <EmptyState>{t("emptyHistory")}</EmptyState>;
	} else {
		chartContent = (
			<HistoryChart
				data={chartData}
				locale={locale}
				selectedCurrency={selectedCurrency}
			/>
		);
	}

	return (
		<div className="grid min-w-0 gap-6">
			<section className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
				<div className="max-w-3xl">
					<h1 className="font-semibold text-2xl sm:text-4xl">{t("historyTitle")}</h1>
					<p className="mt-2 text-muted-foreground">{t("historySubtitle")}</p>
				</div>
				<div className="grid min-w-0 gap-3 sm:grid-cols-3 lg:w-140">
					<SelectField
						label={t("currency")}
						onValueChange={onSelectedCurrencyChange}
						options={currencyOptions}
						value={selectedCurrency}
					/>
					<InputField
						aria-invalid={Boolean(dateRangeError)}
						label={t("from")}
						max={to}
						onChange={(event) => setFrom(event.target.value)}
						type="date"
						value={from}
					/>
					<InputField
						aria-invalid={Boolean(dateRangeError)}
						label={t("to")}
						min={from}
						onChange={(event) => setTo(event.target.value)}
						type="date"
						value={to}
					/>
				</div>
			</section>

			<Card>
				<CardHeader>
					<CardTitle>
						{selectedCurrency} / {bootstrap?.country.baseCurrencyCode ?? "MDL"}
					</CardTitle>
					<CardDescription>
						{formatDate(from, locale)} - {formatDate(to, locale)}
					</CardDescription>
				</CardHeader>
				<CardContent className="min-w-0">{chartContent}</CardContent>
			</Card>

			<Card>
				<CardHeader>
					<div className="flex min-w-0 flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
						<div className="min-w-0">
							<CardTitle>{t("tableHistory")}</CardTitle>
							<CardDescription>{selectedCurrency}</CardDescription>
						</div>
						<ExportMenuButton
							disabled={Boolean(dateRangeError) || isLoading || history.length === 0}
							onExport={handleExport}
							t={t}
						/>
					</div>
				</CardHeader>
				<CardContent>
					<HistoryTable
						emptyLabel={t("emptyHistory")}
						history={dateRangeError ? [] : history}
						locale={locale}
						t={t}
					/>
				</CardContent>
			</Card>
		</div>
	);
}
