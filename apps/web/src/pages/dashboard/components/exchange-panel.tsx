import type { ReactNode } from "react";
import type { Translate } from "@/app-types";
import type { UiLocale } from "@/i18n";
import type { LocationDto } from "@/services/exchange-api";
import type { RateOffer, RateOfferKind } from "../types";
import { formatBestRate } from "../utils";
import { AmountBox } from "./amount-box";
import { RateOfferList } from "./rate-offer-list";

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
	t: Translate;
	title: string;
};

export function ExchangePanel({
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
		<div className="grid min-w-0 gap-4 rounded-lg border bg-card p-4">
			<div className="flex min-w-0 items-center gap-2">
				<span className="inline-flex size-9 items-center justify-center rounded-md bg-secondary text-secondary-foreground">
					{icon}
				</span>
				<div className="min-w-0">
					<h2 className="font-semibold text-lg">{title}</h2>
					<div className="wrap-break-word text-muted-foreground text-sm">
						{formatBestRate(bestRate, kind, baseCurrency, foreignCurrency, locale)}
					</div>
				</div>
			</div>

			<div className="grid min-w-0 items-end gap-3 sm:grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)]">
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
				locale={locale}
				onLocationSelect={onLocationSelect}
				offers={offers}
				t={t}
			/>
		</div>
	);
}
