import { Building2, CalendarDays, MapPin, TrendingDown, TrendingUp } from "lucide-react";
import { useMemo, useState } from "react";
import type { Translate } from "@/app-types";
import { ExportMenuButton } from "@/components/export-menu-button";
import { LocationDialog } from "@/components/location-dialog";
import { RetryState } from "@/components/retry-state";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { type ExportFormat, exportMarketRates } from "@/export-data";
import { formatCompactNumber, formatDate } from "@/format";
import type { UiLocale } from "@/i18n";
import { defaultCurrency } from "@/preferences";
import type { BootstrapDto, LocationDto } from "@/services/exchange-api";
import { getForeignCurrencyOptions } from "@/utils/currency-options";
import { CurrencyRail } from "./components/currency-rail";
import { DashboardSkeleton } from "./components/dashboard-skeleton";
import { ExchangePanel } from "./components/exchange-panel";
import { MetricCard } from "./components/metric-card";
import { OfficialRatesCard } from "./components/official-rates-card";
import { buildRateOffers, convertBaseToForeign, convertForeignToBase, formatConvertedAmount, parseAmount, ratesForCurrency } from "./utils";

type DashboardPageProps = {
	bootstrap: BootstrapDto | null;
	error: string | null;
	isLoading: boolean;
	locale: UiLocale;
	onRefresh: () => void;
	onSelectedCurrencyChange: (currency: string) => void;
	selectedCurrency: string;
	t: Translate;
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
	const currencyOptions = getForeignCurrencyOptions(bootstrap, defaultCurrency);
	const selectedRates = useMemo(() => ratesForCurrency(bootstrap?.marketRates ?? [], selectedCurrency), [bootstrap, selectedCurrency]);
	const buyOffers = useMemo(() => buildRateOffers(selectedRates, "buy"), [selectedRates]);
	const sellOffers = useMemo(() => buildRateOffers(selectedRates, "sell"), [selectedRates]);
	const bestBuyOffer = buyOffers[0] ?? null;
	const bestSellOffer = sellOffers[0] ?? null;
	const baseAmountNumber = parseAmount(baseAmount);
	const foreignAmountNumber = parseAmount(foreignAmount);

	function handleExport(format: ExportFormat) {
		if (!bootstrap) {
			return;
		}

		exportMarketRates({
			bootstrap,
			format,
			rates: selectedRates,
			selectedCurrency
		});
	}

	if (isLoading && !bootstrap) {
		return <DashboardSkeleton />;
	}

	if (error && !bootstrap) {
		return (
			<RetryState
				message={error}
				onRetry={onRefresh}
				retryLabel={t("retry")}
			/>
		);
	}

	if (!bootstrap) {
		return <EmptyState>{t("statusNoData")}</EmptyState>;
	}

	const baseCurrency = bootstrap.country.baseCurrencyCode;
	const buyResult = bestBuyOffer ? convertBaseToForeign(baseAmountNumber, bestBuyOffer.rate) : null;
	const sellResult = bestSellOffer ? convertForeignToBase(foreignAmountNumber, bestSellOffer.rate) : null;

	return (
		<div className="grid min-w-0 gap-6">
			<section className="grid gap-4">
				<div className="max-w-3xl">
					<h1 className="font-semibold text-2xl sm:text-4xl">{t("dashboardTitle")}</h1>
					<p className="mt-2 text-muted-foreground">{t("dashboardSubtitle")}</p>
				</div>

				<Card>
					<CardHeader>
						<div className="flex min-w-0 flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
							<div className="min-w-0">
								<CardTitle>{t("converter")}</CardTitle>
								<CardDescription>
									{bootstrap.city.name}, {bootstrap.country.name} · {t("baseSideLocked")}
								</CardDescription>
							</div>
							<div className="flex flex-wrap items-center gap-2">
								<CurrencyRail
									onSelectedCurrencyChange={onSelectedCurrencyChange}
									options={currencyOptions}
									selectedCurrency={selectedCurrency}
								/>
								<ExportMenuButton
									disabled={selectedRates.length === 0}
									onExport={handleExport}
									t={t}
								/>
							</div>
						</div>
					</CardHeader>
					<CardContent className="grid min-w-0 gap-4 lg:grid-cols-2">
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

			<section className="grid min-w-0 gap-3 sm:grid-cols-2 xl:grid-cols-3">
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
					icon={<MapPin className="size-4" />}
					label={t("baseCurrency")}
					value={baseCurrency}
				/>
			</section>

			<OfficialRatesCard
				locale={locale}
				officialRates={bootstrap.officialRates}
				t={t}
			/>

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
