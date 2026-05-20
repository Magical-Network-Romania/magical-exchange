import { Building2, CalendarDays, MapPin, RefreshCw, TrendingDown, TrendingUp } from "lucide-react";
import { type ReactNode, useMemo, useState } from "react";

import type { BootstrapDto, CurrencyDto, LocationDto, MarketRateDto } from "@/api/exchange";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatCompactNumber, formatDate, formatDateTime, formatRate } from "@/format";
import type { TranslationKey, UiLocale } from "@/i18n";
import { cn } from "@/lib/utils";
import { LocationDialog } from "./location-dialog";

type DashboardPageProps = {
	bootstrap: BootstrapDto | null;
	error: string | null;
	isLoading: boolean;
	locale: UiLocale;
	onRefresh: () => void;
	onSelectedCurrencyChange: (currency: string) => void;
	selectedCurrency: string;
	t: (key: TranslationKey) => string;
};

type RateOfferKind = "buy" | "sell";

type RateOffer = {
	rate: MarketRateDto;
	value: number;
};

export function DashboardPage({
	bootstrap,
	error,
	isLoading,
	locale,
	onRefresh,
	onSelectedCurrencyChange,
	selectedCurrency,
	t
}: DashboardPageProps) {
	const [baseAmount, setBaseAmount] = useState("1000");
	const [foreignAmount, setForeignAmount] = useState("1");
	const [selectedLocation, setSelectedLocation] = useState<LocationDto | null>(null);
	const currencyOptions = getForeignCurrencyOptions(bootstrap);
	const selectedRates = useMemo(() => ratesForCurrency(bootstrap?.marketRates ?? [], selectedCurrency), [bootstrap, selectedCurrency]);
	const buyOffers = useMemo(() => buildRateOffers(selectedRates, "buy"), [selectedRates]);
	const sellOffers = useMemo(() => buildRateOffers(selectedRates, "sell"), [selectedRates]);
	const bestBuyOffer = buyOffers[0] ?? null;
	const bestSellOffer = sellOffers[0] ?? null;
	const baseAmountNumber = parseAmount(baseAmount);
	const foreignAmountNumber = parseAmount(foreignAmount);

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

	const baseCurrency = bootstrap.country.baseCurrencyCode;
	const buyResult = bestBuyOffer ? convertBaseToForeign(baseAmountNumber, bestBuyOffer.rate) : null;
	const sellResult = bestSellOffer ? convertForeignToBase(foreignAmountNumber, bestSellOffer.rate) : null;

	return (
		<div className="grid gap-6">
			<section className="grid gap-4">
				<div className="max-w-3xl">
					<h1 className="font-semibold text-3xl sm:text-4xl">{t("dashboardTitle")}</h1>
					<p className="mt-2 text-muted-foreground">{t("dashboardSubtitle")}</p>
				</div>

				<Card>
					<CardHeader>
						<div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
							<div>
								<CardTitle>{t("converter")}</CardTitle>
								<CardDescription>
									{bootstrap.city.name}, {bootstrap.country.name} · {t("baseSideLocked")}
								</CardDescription>
							</div>
							<CurrencyRail
								onSelectedCurrencyChange={onSelectedCurrencyChange}
								options={currencyOptions}
								selectedCurrency={selectedCurrency}
							/>
						</div>
					</CardHeader>
					<CardContent className="grid gap-4 lg:grid-cols-2">
						<ExchangePanel
							baseCurrency={baseCurrency}
							emptyLabel={t("noMarketRatesForCurrency")}
							foreignCurrency={selectedCurrency}
							heading={`${t("bestBuyCaption")} 1 ${selectedCurrency}`}
							icon={<TrendingDown className="size-4" />}
							inputCurrency={baseCurrency}
							inputLabel={t("youPay")}
							inputValue={baseAmount}
							kind="buy"
							locale={locale}
							onInputChange={setBaseAmount}
							onLocationSelect={setSelectedLocation}
							offers={buyOffers}
							outputCurrency={selectedCurrency}
							outputLabel={t("youReceive")}
							outputValue={formatConvertedAmount(buyResult, locale)}
							title={t("buyForeign")}
							t={t}
						/>
						<ExchangePanel
							baseCurrency={baseCurrency}
							emptyLabel={t("noMarketRatesForCurrency")}
							foreignCurrency={selectedCurrency}
							heading={`${t("bestSellCaption")} 1 ${selectedCurrency}`}
							icon={<TrendingUp className="size-4" />}
							inputCurrency={selectedCurrency}
							inputLabel={t("youPay")}
							inputValue={foreignAmount}
							kind="sell"
							locale={locale}
							onInputChange={setForeignAmount}
							onLocationSelect={setSelectedLocation}
							offers={sellOffers}
							outputCurrency={baseCurrency}
							outputLabel={t("youReceive")}
							outputValue={formatConvertedAmount(sellResult, locale)}
							title={t("sellForeign")}
							t={t}
						/>
					</CardContent>
				</Card>
			</section>

			<section className="grid gap-3 md:grid-cols-4">
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
					value={baseCurrency}
				/>
			</section>

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

type CurrencyRailProps = {
	onSelectedCurrencyChange: (currency: string) => void;
	options: { label: string; value: string }[];
	selectedCurrency: string;
};

function CurrencyRail({ onSelectedCurrencyChange, options, selectedCurrency }: CurrencyRailProps) {
	return (
		<div className="flex max-w-full gap-1 overflow-x-auto rounded-md border bg-muted/35 p-1">
			{options.map((option) => (
				<Button
					className={cn("h-9 px-3", option.value !== selectedCurrency && "bg-card")}
					key={option.value}
					onClick={() => onSelectedCurrencyChange(option.value)}
					type="button"
					variant={option.value === selectedCurrency ? "default" : "ghost"}
				>
					{option.value}
				</Button>
			))}
		</div>
	);
}

type ExchangePanelProps = {
	baseCurrency: string;
	emptyLabel: string;
	foreignCurrency: string;
	heading: string;
	icon: ReactNode;
	inputCurrency: string;
	inputLabel: string;
	inputValue: string;
	kind: RateOfferKind;
	locale: UiLocale;
	onInputChange: (value: string) => void;
	onLocationSelect: (location: LocationDto) => void;
	offers: RateOffer[];
	outputCurrency: string;
	outputLabel: string;
	outputValue: string;
	t: (key: TranslationKey) => string;
	title: string;
};

function ExchangePanel({
	baseCurrency,
	emptyLabel,
	foreignCurrency,
	heading,
	icon,
	inputCurrency,
	inputLabel,
	inputValue,
	kind,
	locale,
	onInputChange,
	onLocationSelect,
	offers,
	outputCurrency,
	outputLabel,
	outputValue,
	t,
	title
}: ExchangePanelProps) {
	const bestRate = offers[0]?.rate ?? null;

	return (
		<div className="grid gap-4 rounded-lg border bg-card p-4">
			<div className="flex items-center gap-2">
				<span className="inline-flex size-9 items-center justify-center rounded-md bg-secondary text-secondary-foreground">{icon}</span>
				<div>
					<h2 className="font-semibold text-lg">{title}</h2>
					<div className="text-muted-foreground text-sm">{formatBestRate(bestRate, kind, baseCurrency, foreignCurrency, locale)}</div>
				</div>
			</div>

			<div className="grid items-end gap-3 sm:grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)]">
				<AmountBox
					currency={inputCurrency}
					label={inputLabel}
					onChange={onInputChange}
					value={inputValue}
				/>
				<div className="flex h-12 items-center justify-center font-semibold text-2xl text-muted-foreground">=</div>
				<AmountBox
					currency={outputCurrency}
					label={outputLabel}
					readOnly
					value={outputValue}
				/>
			</div>

			<RateOfferList
				baseCurrency={baseCurrency}
				emptyLabel={emptyLabel}
				heading={heading}
				kind={kind}
				locale={locale}
				onLocationSelect={onLocationSelect}
				offers={offers}
				t={t}
			/>
		</div>
	);
}

type AmountBoxProps = {
	currency: string;
	label: string;
	onChange?: (value: string) => void;
	readOnly?: boolean;
	value: string;
};

function AmountBox({ currency, label, onChange, readOnly = false, value }: AmountBoxProps) {
	return (
		<label className="grid min-w-0 gap-1.5 text-sm">
			<span className="font-medium text-muted-foreground text-xs uppercase">{label}</span>
			<span className={cn("grid h-12 grid-cols-[minmax(0,1fr)_auto] overflow-hidden rounded-md border bg-card", readOnly && "bg-muted/40")}>
				<input
					aria-label={`${label} ${currency}`}
					className="min-w-0 bg-transparent px-3 text-right font-medium text-lg outline-none"
					inputMode="decimal"
					onChange={(event) => onChange?.(event.target.value)}
					readOnly={readOnly}
					type="text"
					value={value}
				/>
				<span className="flex min-w-14 items-center justify-center border-border border-l bg-muted px-3 font-semibold text-sm">
					{currency}
				</span>
			</span>
		</label>
	);
}

type RateOfferListProps = {
	baseCurrency: string;
	emptyLabel: string;
	heading: string;
	kind: RateOfferKind;
	locale: UiLocale;
	onLocationSelect: (location: LocationDto) => void;
	offers: RateOffer[];
	t: (key: TranslationKey) => string;
};

function RateOfferList({ baseCurrency, emptyLabel, heading, locale, onLocationSelect, offers, t }: RateOfferListProps) {
	if (offers.length === 0) {
		return <EmptyState>{emptyLabel}</EmptyState>;
	}

	return (
		<div className="grid gap-2">
			<div className="font-semibold text-sm">{heading}</div>
			<div className="overflow-hidden rounded-md border">
				{offers.map((offer, index) => (
					<button
						className="grid w-full grid-cols-[minmax(0,1fr)_auto] items-center gap-3 border-border border-b px-3 py-3 text-left transition-colors last:border-b-0 hover:bg-muted/40"
						key={`${offer.rate.location.id}-${offer.rate.currency.code}-${offer.rate.rateType}-${offer.value}`}
						onClick={() => onLocationSelect(offer.rate.location)}
						type="button"
					>
						<span className="min-w-0">
							<span className="flex flex-wrap items-center gap-1.5">
								<span className="font-semibold text-rate-provider">{offer.rate.institution.name}</span>
								<MapPin className="size-3 text-muted-foreground" />
								{index === 0 && <Badge tone="positive">{t("bestRate")}</Badge>}
							</span>
							<span className="block truncate text-muted-foreground text-xs">{offer.rate.location.name}</span>
						</span>
						<span className="text-right">
							<span className="block font-semibold">
								{formatRate(offer.value, locale)} {baseCurrency}
							</span>
							<span className="block text-muted-foreground text-xs">{formatDateTime(offer.rate.fetchedAt, locale)}</span>
						</span>
					</button>
				))}
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

function ratesForCurrency(rates: MarketRateDto[], currency: string) {
	return rates.filter((rate) => rate.currency.code === currency);
}

function buildRateOffers(rates: MarketRateDto[], kind: RateOfferKind) {
	const offers: RateOffer[] = [];

	for (const rate of rates) {
		const value = kind === "buy" ? rate.sellRate : rate.buyRate;

		if (value !== null) {
			offers.push({ rate, value });
		}
	}

	return offers.sort((left, right) => compareRateOffers(left, right, kind));
}

function compareRateOffers(left: RateOffer, right: RateOffer, kind: RateOfferKind) {
	const rateOrder = kind === "buy" ? left.value - right.value : right.value - left.value;

	if (rateOrder !== 0) {
		return rateOrder;
	}

	return left.rate.institution.name.localeCompare(right.rate.institution.name) || left.rate.location.name.localeCompare(right.rate.location.name);
}

function parseAmount(value: string) {
	const normalizedValue = value.trim().replace(",", ".");

	if (!normalizedValue) {
		return null;
	}

	const parsedValue = Number(normalizedValue);

	return Number.isFinite(parsedValue) ? parsedValue : null;
}

function convertBaseToForeign(amount: number | null, rate: MarketRateDto) {
	if (amount === null || rate.sellRate === null || rate.sellRate <= 0) {
		return null;
	}

	return (amount * rate.unit) / rate.sellRate;
}

function convertForeignToBase(amount: number | null, rate: MarketRateDto) {
	if (amount === null || rate.buyRate === null || rate.buyRate <= 0) {
		return null;
	}

	return (amount / rate.unit) * rate.buyRate;
}

function formatConvertedAmount(value: number | null, locale: UiLocale) {
	if (value === null || !Number.isFinite(value)) {
		return "—";
	}

	return formatRate(value, locale);
}

function formatBestRate(rate: MarketRateDto | null, kind: RateOfferKind, baseCurrency: string, foreignCurrency: string, locale: UiLocale) {
	if (!rate) {
		return "—";
	}

	const value = kind === "buy" ? rate.sellRate : rate.buyRate;

	if (value === null) {
		return "—";
	}

	return `${formatCompactNumber(rate.unit, locale)} ${foreignCurrency} = ${formatRate(value, locale)} ${baseCurrency}`;
}
