import { type ReactNode, useEffect, useMemo, useState } from "react";
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { type BootstrapDto, fetchOfficialRateHistory, isAbortError, type OfficialRateHistoryPoint } from "@/api/exchange";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { InputField, SelectField } from "@/components/ui/field";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { daysAgoIsoDate, formatDate, formatDateTime, formatRate, todayIsoDate } from "@/format";
import type { TranslationKey, UiLocale } from "@/i18n";

type HistoryPageProps = {
	bootstrap: BootstrapDto | null;
	country: string;
	locale: UiLocale;
	onSelectedCurrencyChange: (currency: string) => void;
	selectedCurrency: string;
	t: (key: TranslationKey) => string;
};

type HistoryChartPoint = {
	date: string;
	formattedDate: string;
	rate: number;
};

export function HistoryPage({ bootstrap, country, locale, onSelectedCurrencyChange, selectedCurrency, t }: HistoryPageProps) {
	const [from, setFrom] = useState(daysAgoIsoDate(30));
	const [to, setTo] = useState(todayIsoDate());
	const [history, setHistory] = useState<OfficialRateHistoryPoint[]>([]);
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState<string | null>(null);
	const currencyOptions = useMemo(() => {
		if (!bootstrap) {
			return [{ label: selectedCurrency, value: selectedCurrency }];
		}

		return bootstrap.currencies
			.filter((currency) => currency.code !== bootstrap.country.baseCurrencyCode)
			.map((currency) => ({
				label: `${currency.code} · ${currency.name}`,
				value: currency.code
			}));
	}, [bootstrap, selectedCurrency]);
	const chartData = history.map((point) => ({
		date: point.rateDate,
		formattedDate: formatDate(point.rateDate, locale),
		rate: point.rate / point.unit
	}));
	let chartContent: ReactNode;

	if (isLoading) {
		chartContent = <Skeleton className="h-[320px]" />;
	} else if (error) {
		chartContent = <EmptyState>{error}</EmptyState>;
	} else if (history.length === 0) {
		chartContent = <EmptyState>{t("emptyHistory")}</EmptyState>;
	} else {
		chartContent = (
			<div className="h-[320px]">
				<ResponsiveContainer
					height="100%"
					width="100%"
				>
					<LineChart data={chartData}>
						<CartesianGrid
							stroke="var(--border)"
							strokeDasharray="3 3"
						/>
						<XAxis
							dataKey="formattedDate"
							minTickGap={28}
							stroke="var(--muted-foreground)"
							tickLine={false}
						/>
						<YAxis
							domain={["auto", "auto"]}
							stroke="var(--muted-foreground)"
							tickFormatter={(value: number) => formatRate(value, locale, 3)}
							tickLine={false}
							width={72}
						/>
						<Tooltip
							content={({ active, payload }) => (
								<ChartTooltip
									active={Boolean(active)}
									locale={locale}
									payload={payload?.[0]?.payload as HistoryChartPoint | undefined}
									selectedCurrency={selectedCurrency}
								/>
							)}
						/>
						<Line
							dataKey="rate"
							dot={false}
							stroke="var(--primary)"
							strokeWidth={2}
							type="monotone"
						/>
					</LineChart>
				</ResponsiveContainer>
			</div>
		);
	}

	useEffect(() => {
		const controller = new AbortController();

		setIsLoading(true);
		setError(null);

		fetchOfficialRateHistory(country, selectedCurrency, from, to, { signal: controller.signal })
			.then((points) => {
				setHistory(points);
				setIsLoading(false);
			})
			.catch((caughtError: unknown) => {
				if (!isAbortError(caughtError)) {
					setError(caughtError instanceof Error ? caughtError.message : "Unknown API error");
					setHistory([]);
					setIsLoading(false);
				}
			});

		return () => controller.abort();
	}, [country, from, selectedCurrency, to]);

	return (
		<div className="grid gap-6">
			<section className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
				<div className="max-w-3xl">
					<h1 className="font-semibold text-3xl sm:text-4xl">{t("historyTitle")}</h1>
					<p className="mt-2 text-muted-foreground">{t("historySubtitle")}</p>
				</div>
				<div className="grid gap-3 sm:grid-cols-3 lg:w-[560px]">
					<SelectField
						label={t("currency")}
						onValueChange={onSelectedCurrencyChange}
						options={currencyOptions}
						value={selectedCurrency}
					/>
					<InputField
						label={t("from")}
						onChange={(event) => setFrom(event.target.value)}
						type="date"
						value={from}
					/>
					<InputField
						label={t("to")}
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
				<CardContent>{chartContent}</CardContent>
			</Card>

			<Card>
				<CardHeader>
					<CardTitle>{t("tableHistory")}</CardTitle>
					<CardDescription>{selectedCurrency}</CardDescription>
				</CardHeader>
				<CardContent>
					{history.length === 0 ? (
						<EmptyState>{t("emptyHistory")}</EmptyState>
					) : (
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
					)}
				</CardContent>
			</Card>
		</div>
	);
}

type ChartTooltipProps = {
	active: boolean;
	locale: UiLocale;
	payload: HistoryChartPoint | undefined;
	selectedCurrency: string;
};

function ChartTooltip({ active, locale, payload, selectedCurrency }: ChartTooltipProps) {
	if (!active || !payload) {
		return null;
	}

	return (
		<div className="rounded-md border bg-card px-3 py-2 text-sm shadow-md">
			<div className="text-muted-foreground">{payload.formattedDate}</div>
			<div className="font-semibold">
				{formatRate(payload.rate, locale)} {selectedCurrency}
			</div>
		</div>
	);
}
