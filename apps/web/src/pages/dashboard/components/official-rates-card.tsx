import type { Translate } from "@/app-types";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatRate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { OfficialRatesDto } from "@/services/exchange-api";

type OfficialRatesCardProps = {
	locale: UiLocale;
	officialRates: OfficialRatesDto;
	t: Translate;
};

export function OfficialRatesCard({ locale, officialRates, t }: OfficialRatesCardProps) {
	return (
		<Card>
			<CardHeader>
				<CardTitle>{t("cardOfficialTitle")}</CardTitle>
				<CardDescription>{t("cardOfficialDescription")}</CardDescription>
			</CardHeader>
			<CardContent>
				{officialRates.rates.length === 0 ? (
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
							{officialRates.rates.map((rate) => (
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
	);
}
