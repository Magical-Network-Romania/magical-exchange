import { Building2, CalendarDays, MapPin, RefreshCw, TrendingDown, TrendingUp } from "lucide-react";
import { type ReactNode, useMemo, useState } from "react";

import type { BestMarketRatesDto, BestRateOperation, BootstrapDto, CurrencyDto, LocationDto, MarketRateDto } from "@/api/exchange";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { InputField, SelectField } from "@/components/ui/field";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatCompactNumber, formatDate, formatDateTime, formatRate } from "@/format";
import type { TranslationKey, UiLocale } from "@/i18n";
import { LocationDialog } from "./location-dialog";

type DashboardPageProps = {
	bestRates: BestMarketRatesDto | null;
	bootstrap: BootstrapDto | null;
	error: string | null;
	isBestLoading: boolean;
	isLoading: boolean;
	locale: UiLocale;
	onOperationChange: (operation: BestRateOperation) => void;
	onRefresh: () => void;
	onSelectedCurrencyChange: (currency: string) => void;
	operation: BestRateOperation;
	selectedCurrency: string;
	t: (key: TranslationKey) => string;
};

export function DashboardPage({
	bestRates,
	bootstrap,
	error,
	isBestLoading,
	isLoading,
	locale,
	onOperationChange,
	onRefresh,
	onSelectedCurrencyChange,
	operation,
	selectedCurrency,
	t
}: DashboardPageProps) {
	const [amount, setAmount] = useState("100");
	const [selectedLocation, setSelectedLocation] = useState<LocationDto | null>(null);
	const officialRate = bootstrap?.officialRates.rates.find((rate) => rate.currency.code === selectedCurrency) ?? null;
	const amountNumber = Number(amount);
	const unitRate = officialRate ? officialRate.rate / officialRate.unit : null;
	const currencyOptions = getForeignCurrencyOptions(bootstrap);
	const groupedMarketRates = useMemo(() => groupMarketRates(bootstrap?.marketRates ?? []), [bootstrap]);

	if (isLoading && !bootstrap) {
		return <DashboardSkeleton />;
	}

	if (error && !bootstrap) {
		return (
			<EmptyState>
				<div className="grid justify-items-center gap-3">
					<span>{error}</span>
					<Button
						onClick={onRefresh}
						type="button"
						variant="outline"
					>
						<RefreshCw className="size-4" />
						{t("retry")}
					</Button>
				</div>
			</EmptyState>
		);
	}

	if (!bootstrap) {
		return <EmptyState>{t("statusNoData")}</EmptyState>;
	}

	return (
		<div className="grid gap-6">
			<section className="grid gap-3">
				<div className="max-w-3xl">
					<h1 className="font-semibold text-3xl sm:text-4xl">{t("dashboardTitle")}</h1>
					<p className="mt-2 text-muted-foreground">{t("dashboardSubtitle")}</p>
				</div>
				<div className="grid gap-3 md:grid-cols-4">
					<MetricCard
						icon={<CalendarDays className="size-4" />}
						label={t("latestOfficialDate")}
						value={formatDate(bootstrap.officialRates.rateDate, locale)}
					/>
					<MetricCard
						icon={<Building2 className="size-4" />}
						label={t("locations")}
						value={formatCompactNumber(bootstrap.locations.length, locale)}
					/>
					<MetricCard
						icon={<TrendingUp className="size-4" />}
						label={t("marketRates")}
						value={formatCompactNumber(bootstrap.marketRates.length, locale)}
					/>
					<MetricCard
						icon={<MapPin className="size-4" />}
						label={t("baseCurrency")}
						value={bootstrap.country.baseCurrencyCode}
					/>
				</div>
			</section>

			<section className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
				<Card>
					<CardHeader>
						<CardTitle>{t("cardOfficialTitle")}</CardTitle>
						<CardDescription>{t("cardOfficialDescription")}</CardDescription>
					</CardHeader>
					<CardContent>
						{bootstrap.officialRates.rates.length === 0 ? (
							<EmptyState>{t("emptyOfficialRates")}</EmptyState>
						) : (
							<Table>
								<TableHeader>
									<TableRow>
										<TableHead>{t("currency")}</TableHead>
										<TableHead>{t("unit")}</TableHead>
										<TableHead>{t("rate")}</TableHead>
									</TableRow>
								</TableHeader>
								<TableBody>
									{bootstrap.officialRates.rates.map((rate) => (
										<TableRow key={rate.currency.code}>
											<TableCell>
												<div className="font-medium">{rate.currency.code}</div>
												<div className="text-muted-foreground text-xs">{rate.currency.name}</div>
											</TableCell>
											<TableCell>{rate.unit}</TableCell>
											<TableCell className="font-semibold">{formatRate(rate.rate, locale)}</TableCell>
										</TableRow>
									))}
								</TableBody>
							</Table>
						)}
					</CardContent>
				</Card>

				<div className="grid gap-6">
					<Card>
						<CardHeader>
							<CardTitle>{t("converter")}</CardTitle>
							<CardDescription>
								{bootstrap.country.baseCurrencyCode} / {selectedCurrency}
							</CardDescription>
						</CardHeader>
						<CardContent className="grid gap-4">
							<div className="grid gap-3 sm:grid-cols-2">
								<InputField
									inputMode="decimal"
									label={t("amount")}
									onChange={(event) => setAmount(event.target.value)}
									type="number"
									value={amount}
								/>
								<SelectField
									label={t("currency")}
									onValueChange={onSelectedCurrencyChange}
									options={currencyOptions}
									value={selectedCurrency}
								/>
							</div>
							{unitRate && !Number.isNaN(amountNumber) ? (
								<div className="grid gap-3 sm:grid-cols-2">
									<ConversionResult
										label={`${amount} ${bootstrap.country.baseCurrencyCode}`}
										value={`${formatRate(amountNumber / unitRate, locale)} ${selectedCurrency}`}
									/>
									<ConversionResult
										label={`${amount} ${selectedCurrency}`}
										value={`${formatRate(amountNumber * unitRate, locale)} ${bootstrap.country.baseCurrencyCode}`}
									/>
								</div>
							) : (
								<EmptyState>{t("noConverterRate")}</EmptyState>
							)}
						</CardContent>
					</Card>

					<Card>
						<CardHeader>
							<div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
								<div>
									<CardTitle>{t("bestOffers")}</CardTitle>
									<CardDescription>{selectedCurrency} · CASH</CardDescription>
								</div>
								<div className="flex flex-wrap gap-2">
									<OperationButton
										active={operation === "BUY_FOREIGN_CURRENCY"}
										icon={<TrendingDown className="size-4" />}
										label={t("buyForeign")}
										onClick={() => onOperationChange("BUY_FOREIGN_CURRENCY")}
									/>
									<OperationButton
										active={operation === "SELL_FOREIGN_CURRENCY"}
										icon={<TrendingUp className="size-4" />}
										label={t("sellForeign")}
										onClick={() => onOperationChange("SELL_FOREIGN_CURRENCY")}
									/>
								</div>
							</div>
						</CardHeader>
						<CardContent>
							<BestRatesList
								bestRates={bestRates}
								isLoading={isBestLoading}
								locale={locale}
								operation={operation}
								t={t}
							/>
						</CardContent>
					</Card>
				</div>
			</section>

			<section className="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]">
				<Card>
					<CardHeader>
						<CardTitle>{t("marketRates")}</CardTitle>
						<CardDescription>
							{bootstrap.city.name}, {bootstrap.country.name}
						</CardDescription>
					</CardHeader>
					<CardContent>
						{groupedMarketRates.length === 0 ? (
							<EmptyState>{t("emptyMarketRates")}</EmptyState>
						) : (
							<div className="grid gap-4">
								{groupedMarketRates.map((group) => (
									<div
										className="rounded-lg border"
										key={group.location.id}
									>
										<div className="flex flex-col gap-3 border-border border-b p-4 sm:flex-row sm:items-center sm:justify-between">
											<div>
												<div className="font-semibold">{group.location.name}</div>
												<div className="text-muted-foreground text-sm">{group.location.address}</div>
											</div>
											<Button
												onClick={() => setSelectedLocation(group.location)}
												type="button"
												variant="outline"
											>
												{t("details")}
											</Button>
										</div>
										<Table>
											<TableHeader>
												<TableRow>
													<TableHead>{t("currency")}</TableHead>
													<TableHead>{t("buy")}</TableHead>
													<TableHead>{t("sell")}</TableHead>
													<TableHead>{t("fetched")}</TableHead>
												</TableRow>
											</TableHeader>
											<TableBody>
												{group.rates.map((rate) => (
													<TableRow key={`${rate.location.id}-${rate.currency.code}-${rate.rateType}`}>
														<TableCell>
															<div className="font-medium">{rate.currency.code}</div>
															<div className="text-muted-foreground text-xs">{rate.currency.name}</div>
														</TableCell>
														<TableCell>{formatRate(rate.buyRate, locale)}</TableCell>
														<TableCell>{formatRate(rate.sellRate, locale)}</TableCell>
														<TableCell>{formatDateTime(rate.fetchedAt, locale)}</TableCell>
													</TableRow>
												))}
											</TableBody>
										</Table>
									</div>
								))}
							</div>
						)}
					</CardContent>
				</Card>

				<Card>
					<CardHeader>
						<CardTitle>{t("locations")}</CardTitle>
						<CardDescription>{bootstrap.city.name}</CardDescription>
					</CardHeader>
					<CardContent className="grid gap-3">
						{bootstrap.locations.length === 0 ? (
							<EmptyState>{t("emptyLocations")}</EmptyState>
						) : (
							bootstrap.locations.map((location) => (
								<button
									className="rounded-lg border bg-card p-3 text-left transition-colors hover:bg-muted/45"
									key={location.id}
									onClick={() => setSelectedLocation(location)}
									type="button"
								>
									<div className="flex items-start justify-between gap-3">
										<div>
											<div className="font-medium">{location.name}</div>
											<div className="mt-1 text-muted-foreground text-sm">{location.address}</div>
										</div>
										<Badge tone="secondary">{location.institution.name}</Badge>
									</div>
								</button>
							))
						)}
					</CardContent>
				</Card>
			</section>

			<LocationDialog
				location={selectedLocation}
				onOpenChange={(open) => {
					if (!open) {
						setSelectedLocation(null);
					}
				}}
				t={t}
			/>
		</div>
	);
}

function DashboardSkeleton() {
	return (
		<div className="grid gap-6">
			<Skeleton className="h-24" />
			<div className="grid gap-6 xl:grid-cols-2">
				<Skeleton className="h-96" />
				<Skeleton className="h-96" />
			</div>
		</div>
	);
}

type MetricCardProps = {
	icon: ReactNode;
	label: string;
	value: string;
};

function MetricCard({ icon, label, value }: MetricCardProps) {
	return (
		<div className="rounded-lg border bg-card p-4">
			<div className="flex items-center gap-2 text-muted-foreground text-sm">
				{icon}
				<span>{label}</span>
			</div>
			<div className="mt-2 font-semibold text-2xl">{value}</div>
		</div>
	);
}

type ConversionResultProps = {
	label: string;
	value: string;
};

function ConversionResult({ label, value }: ConversionResultProps) {
	return (
		<div className="rounded-lg border bg-muted/30 p-4">
			<div className="text-muted-foreground text-sm">{label}</div>
			<div className="mt-1 font-semibold text-2xl">{value}</div>
		</div>
	);
}

type OperationButtonProps = {
	active: boolean;
	icon: ReactNode;
	label: string;
	onClick: () => void;
};

function OperationButton({ active, icon, label, onClick }: OperationButtonProps) {
	return (
		<Button
			onClick={onClick}
			type="button"
			variant={active ? "default" : "outline"}
		>
			{icon}
			{label}
		</Button>
	);
}

type BestRatesListProps = {
	bestRates: BestMarketRatesDto | null;
	isLoading: boolean;
	locale: UiLocale;
	operation: BestRateOperation;
	t: (key: TranslationKey) => string;
};

function BestRatesList({ bestRates, isLoading, locale, operation, t }: BestRatesListProps) {
	if (isLoading) {
		return (
			<div className="grid gap-3">
				<Skeleton className="h-16" />
				<Skeleton className="h-16" />
				<Skeleton className="h-16" />
			</div>
		);
	}

	if (!bestRates || bestRates.rates.length === 0) {
		return <EmptyState>{t("emptyBestRates")}</EmptyState>;
	}

	const displayRates = bestRates.rates.slice(0, 5);

	return (
		<div className="grid gap-3">
			{displayRates.map((rate) => {
				const bestValue = operation === "BUY_FOREIGN_CURRENCY" ? rate.sellRate : rate.buyRate;

				return (
					<div
						className="flex flex-col gap-3 rounded-lg border p-3 sm:flex-row sm:items-center sm:justify-between"
						key={`${rate.location.id}-${rate.currency.code}-${operation}`}
					>
						<div>
							<div className="font-semibold">{rate.location.name}</div>
							<div className="text-muted-foreground text-sm">{rate.institution.name}</div>
						</div>
						<div className="flex items-center gap-2">
							<Badge tone="positive">{formatRate(bestValue, locale)}</Badge>
							<span className="text-muted-foreground text-sm">{rate.currency.code}</span>
						</div>
					</div>
				);
			})}
		</div>
	);
}

type MarketRateGroup = {
	location: LocationDto;
	rates: MarketRateDto[];
};

function getForeignCurrencyOptions(bootstrap: BootstrapDto | null) {
	if (!bootstrap) {
		return [{ label: "EUR", value: "EUR" }];
	}

	return bootstrap.currencies.filter((currency) => currency.code !== bootstrap.country.baseCurrencyCode).map(currencyOption);
}

function currencyOption(currency: CurrencyDto) {
	return {
		label: `${currency.code} · ${currency.name}`,
		value: currency.code
	};
}

function groupMarketRates(rates: MarketRateDto[]) {
	const groups = new Map<string, MarketRateGroup>();

	for (const rate of rates) {
		const existingGroup = groups.get(rate.location.id);

		if (existingGroup) {
			existingGroup.rates.push(rate);
		} else {
			groups.set(rate.location.id, {
				location: rate.location,
				rates: [rate]
			});
		}
	}

	return [...groups.values()].map((group) => ({
		...group,
		rates: [...group.rates].sort((left, right) => left.currency.code.localeCompare(right.currency.code))
	}));
}
