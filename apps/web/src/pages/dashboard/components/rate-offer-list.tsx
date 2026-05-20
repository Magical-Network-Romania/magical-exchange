import { MapPin } from "lucide-react";
import type { Translate } from "@/app-types";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/ui/empty-state";
import { formatDateTime, formatRate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { LocationDto } from "@/services/exchange-api";
import type { RateOffer } from "../types";

type RateOfferListProps = {
	baseCurrency: string;
	emptyLabel: string;
	heading: string;
	locale: UiLocale;
	onLocationSelect: (location: LocationDto) => void;
	offers: RateOffer[];
	t: Translate;
};

export function RateOfferList({ baseCurrency, emptyLabel, heading, locale, onLocationSelect, offers, t }: RateOfferListProps) {
	if (offers.length === 0) {
		return <EmptyState>{emptyLabel}</EmptyState>;
	}

	return (
		<div className="grid gap-2">
			<div className="wrap-break-word font-semibold text-sm">{heading}</div>
			<div className="overflow-hidden rounded-md border">
				{offers.map((offer, index) => (
					<button
						className="grid w-full grid-cols-1 items-start gap-2 border-border border-b px-3 py-3 text-left transition-colors last:border-b-0 hover:bg-muted/40 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-center sm:gap-3"
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
						<span className="text-left sm:text-right">
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
