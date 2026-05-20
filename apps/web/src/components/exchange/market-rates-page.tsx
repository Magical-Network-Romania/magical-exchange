import { MapPin, RefreshCw } from "lucide-react";
import { useMemo, useState } from "react";

import type { BootstrapDto, LocationDto, MarketRateDto } from "@/api/exchange";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDateTime, formatRate } from "@/format";
import type { TranslationKey, UiLocale } from "@/i18n";
import { LocationDialog } from "./location-dialog";

type MarketRatesPageProps = {
	bootstrap: BootstrapDto | null;
	error: string | null;
	isLoading: boolean;
	locale: UiLocale;
	onRefresh: () => void;
	t: (key: TranslationKey) => string;
};

type MarketRateGroup = {
	location: LocationDto;
	rates: MarketRateDto[];
};

export function MarketRatesPage({ bootstrap, error, isLoading, locale, onRefresh, t }: MarketRatesPageProps) {
	const [selectedLocation, setSelectedLocation] = useState<LocationDto | null>(null);
	const groupedMarketRates = useMemo(() => groupMarketRates(bootstrap?.marketRates ?? []), [bootstrap]);

	if (isLoading && !bootstrap) {
		return <MarketRatesSkeleton />;
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
			<section className="max-w-3xl">
				<h1 className="font-semibold text-3xl sm:text-4xl">{t("allRatesTitle")}</h1>
				<p className="mt-2 text-muted-foreground">
					{bootstrap.city.name}, {bootstrap.country.name}
				</p>
			</section>

			<Card>
				<CardHeader>
					<CardTitle>{t("allRates")}</CardTitle>
					<CardDescription>{t("allRatesSubtitle")}</CardDescription>
				</CardHeader>
				<CardContent>
					{groupedMarketRates.length === 0 ? (
						<EmptyState>{t("emptyMarketRates")}</EmptyState>
					) : (
						<div className="grid gap-4">
							{groupedMarketRates.map((group) => (
								<div
									className="overflow-hidden rounded-lg border bg-card"
									key={group.location.id}
								>
									<button
										className="flex w-full flex-col gap-3 border-border border-b p-4 text-left transition-colors hover:bg-muted/40 sm:flex-row sm:items-center sm:justify-between"
										onClick={() => setSelectedLocation(group.location)}
										type="button"
									>
										<span className="min-w-0">
											<span className="flex flex-wrap items-center gap-2">
												<span className="font-semibold">{group.location.institution.name}</span>
												<Badge tone="secondary">{group.rates.length}</Badge>
											</span>
											<span className="mt-1 flex items-center gap-1 text-muted-foreground text-sm">
												<MapPin className="size-3" />
												<span className="truncate">{group.location.name}</span>
											</span>
										</span>
										<span className="text-muted-foreground text-sm">{group.location.address}</span>
									</button>
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
												<TableRow
													className="cursor-pointer"
													key={`${rate.location.id}-${rate.currency.code}-${rate.rateType}`}
													onClick={() => setSelectedLocation(rate.location)}
												>
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

function MarketRatesSkeleton() {
	return (
		<div className="grid gap-6">
			<Skeleton className="h-24" />
			<Skeleton className="h-96" />
		</div>
	);
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

	return [...groups.values()]
		.map((group) => ({
			...group,
			rates: [...group.rates].sort((left, right) => left.currency.code.localeCompare(right.currency.code))
		}))
		.sort((left, right) => left.location.institution.name.localeCompare(right.location.institution.name));
}
