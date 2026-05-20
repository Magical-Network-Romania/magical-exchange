import { MapPin } from "lucide-react";
import type { Translate } from "@/app-types";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/ui/empty-state";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDateTime, formatRate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { LocationDto } from "@/services/exchange-api";
import type { MarketRateGroup } from "../types";

type MarketRatesListProps = {
	emptyLabel: string;
	groups: MarketRateGroup[];
	locale: UiLocale;
	onLocationSelect: (location: LocationDto) => void;
	t: Translate;
};

export function MarketRatesList({ emptyLabel, groups, locale, onLocationSelect, t }: MarketRatesListProps) {
	if (groups.length === 0) {
		return <EmptyState>{emptyLabel}</EmptyState>;
	}

	return (
		<div className="grid min-w-0 gap-4">
			{groups.map((group) => (
				<div
					className="min-w-0 overflow-hidden rounded-lg border bg-card"
					key={group.location.id}
				>
					<button
						className="grid w-full grid-cols-1 gap-3 border-border border-b p-4 text-left transition-colors hover:bg-muted/40 sm:grid-cols-[minmax(0,1fr)_minmax(12rem,0.8fr)] sm:items-center"
						onClick={() => onLocationSelect(group.location)}
						type="button"
					>
						<span className="min-w-0">
							<span className="flex flex-wrap items-center gap-2">
								<span className="min-w-0 wrap-break-word font-semibold">{group.location.institution.name}</span>
								<Badge tone="secondary">{group.rates.length}</Badge>
							</span>
							<span className="mt-1 flex items-center gap-1 text-muted-foreground text-sm">
								<MapPin className="size-3" />
								<span className="truncate">{group.location.name}</span>
							</span>
						</span>
						<span className="wrap-break-word text-muted-foreground text-sm sm:text-right">{group.location.address}</span>
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
									onClick={() => onLocationSelect(rate.location)}
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
	);
}
