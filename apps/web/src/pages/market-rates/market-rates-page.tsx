import { useMemo, useState } from "react";
import type { Translate } from "@/app-types";
import { ExportMenuButton } from "@/components/export-menu-button";
import { LocationDialog } from "@/components/location-dialog";
import { RetryState } from "@/components/retry-state";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { type ExportFormat, exportMarketRates } from "@/export-data";
import type { UiLocale } from "@/i18n";
import type { BootstrapDto, LocationDto } from "@/services/exchange-api";
import { MarketRatesList } from "./components/market-rates-list";
import { MarketRatesSkeleton } from "./components/market-rates-skeleton";
import { groupMarketRates } from "./utils";

type MarketRatesPageProps = {
	bootstrap: BootstrapDto | null;
	error: string | null;
	isLoading: boolean;
	locale: UiLocale;
	onRefresh: () => void;
	t: Translate;
};

export function MarketRatesPage({ bootstrap, error, isLoading, locale, onRefresh, t }: MarketRatesPageProps) {
	const [selectedLocation, setSelectedLocation] = useState<LocationDto | null>(null);
	const groupedMarketRates = useMemo(() => groupMarketRates(bootstrap?.marketRates ?? []), [bootstrap]);

	function handleExport(format: ExportFormat) {
		if (!bootstrap) {
			return;
		}

		exportMarketRates({
			bootstrap,
			format,
			rates: bootstrap.marketRates,
			selectedCurrency: null
		});
	}

	if (isLoading && !bootstrap) {
		return <MarketRatesSkeleton />;
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

	return (
		<div className="grid min-w-0 gap-6">
			<section className="max-w-3xl">
				<h1 className="font-semibold text-2xl sm:text-4xl">{t("allRatesTitle")}</h1>
				<p className="mt-2 text-muted-foreground">
					{bootstrap.city.name}, {bootstrap.country.name}
				</p>
			</section>

			<Card>
				<CardHeader>
					<div className="flex min-w-0 flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
						<div className="min-w-0">
							<CardTitle>{t("allRates")}</CardTitle>
							<CardDescription>{t("allRatesSubtitle")}</CardDescription>
						</div>
						<ExportMenuButton
							disabled={bootstrap.marketRates.length === 0}
							onExport={handleExport}
							t={t}
						/>
					</div>
				</CardHeader>
				<CardContent>
					<MarketRatesList
						emptyLabel={t("emptyMarketRates")}
						groups={groupedMarketRates}
						locale={locale}
						onLocationSelect={setSelectedLocation}
						t={t}
					/>
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
