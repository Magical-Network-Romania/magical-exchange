import { BarChart3, Coins, RefreshCw } from "lucide-react";
import type { ReactNode } from "react";

import type { CityDto, CountryDto } from "@/api/exchange";
import { Button } from "@/components/ui/button";
import { SelectField } from "@/components/ui/field";
import { localeLabels, supportedLocales, type TranslationKey, type UiLocale } from "@/i18n";
import { cn } from "@/lib/utils";

export type AppRoute = "dashboard" | "history";

type AppShellProps = {
	children: ReactNode;
	cities: CityDto[];
	city: string;
	countries: CountryDto[];
	country: string;
	locale: UiLocale;
	onCityChange: (city: string) => void;
	onCountryChange: (country: string) => void;
	onLocaleChange: (locale: UiLocale) => void;
	onNavigate: (route: AppRoute) => void;
	onRefresh: () => void;
	route: AppRoute;
	t: (key: TranslationKey) => string;
};

export function AppShell({
	children,
	cities,
	city,
	countries,
	country,
	locale,
	onCityChange,
	onCountryChange,
	onLocaleChange,
	onNavigate,
	onRefresh,
	route,
	t
}: AppShellProps) {
	const countryOptions =
		countries.length > 0 ? countries.map((item) => ({ label: item.name, value: item.code })) : [{ label: country, value: country }];
	const cityOptions = cities.length > 0 ? cities.map((item) => ({ label: item.name, value: item.slug })) : [{ label: city, value: city }];
	const localeOptions = supportedLocales.map((item) => ({ label: localeLabels[item], value: item }));

	return (
		<div className="min-h-screen bg-background">
			<header className="border-border border-b bg-background/95">
				<div className="mx-auto flex w-full max-w-7xl flex-col gap-5 px-4 py-4 sm:px-6 lg:px-8">
					<div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
						<div className="flex items-center gap-3">
							<img
								alt=""
								className="size-11 rounded-md"
								src="/logo.svg"
							/>
							<div>
								<div className="font-semibold text-lg">{t("appName")}</div>
								<div className="text-muted-foreground text-sm">exchange.magical.md</div>
							</div>
						</div>

						<div className="grid gap-3 sm:grid-cols-3 lg:w-[520px]">
							<SelectField
								label={t("country")}
								onValueChange={onCountryChange}
								options={countryOptions}
								value={country}
							/>
							<SelectField
								label={t("city")}
								onValueChange={onCityChange}
								options={cityOptions}
								value={city}
							/>
							<SelectField
								label={t("language")}
								onValueChange={(value) => onLocaleChange(value as UiLocale)}
								options={localeOptions}
								value={locale}
							/>
						</div>
					</div>

					<div className="flex flex-wrap items-center justify-between gap-3">
						<nav className="flex gap-2">
							<NavButton
								active={route === "dashboard"}
								icon={<Coins className="size-4" />}
								label={t("currentRates")}
								onClick={() => onNavigate("dashboard")}
							/>
							<NavButton
								active={route === "history"}
								icon={<BarChart3 className="size-4" />}
								label={t("history")}
								onClick={() => onNavigate("history")}
							/>
						</nav>
						<Button
							onClick={onRefresh}
							type="button"
							variant="outline"
						>
							<RefreshCw className="size-4" />
							{t("refresh")}
						</Button>
					</div>
				</div>
			</header>

			<main className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">{children}</main>
		</div>
	);
}

type NavButtonProps = {
	active: boolean;
	icon: ReactNode;
	label: string;
	onClick: () => void;
};

function NavButton({ active, icon, label, onClick }: NavButtonProps) {
	return (
		<Button
			aria-current={active ? "page" : undefined}
			className={cn(!active && "text-muted-foreground")}
			onClick={onClick}
			type="button"
			variant={active ? "default" : "ghost"}
		>
			{icon}
			{label}
		</Button>
	);
}
