import { BarChart3, Coins, Moon, RefreshCw, Sun, Table2 } from "lucide-react";
import type { ReactNode } from "react";

import { Button } from "@/components/ui/button";
import { SelectField } from "@/components/ui/field";
import { isUiLocale, localeOptions, type TranslationKey, type UiLocale } from "@/i18n";
import { cn } from "@/lib/utils";
import type { AppRoute } from "@/routing";
import type { CityDto, CountryDto } from "@/services/exchange-api";

export type ThemeMode = "dark" | "light";

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
	onThemeChange: () => void;
	route: AppRoute;
	t: (key: TranslationKey) => string;
	theme: ThemeMode;
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
	onThemeChange,
	route,
	t,
	theme
}: AppShellProps) {
	const countryOptions =
		countries.length > 0 ? countries.map((item) => ({ label: item.name, value: item.code })) : [{ label: country, value: country }];
	const cityOptions = cities.length > 0 ? cities.map((item) => ({ label: item.name, value: item.slug })) : [{ label: city, value: city }];

	return (
		<div className="min-h-screen bg-background">
			<header className="border-border border-b bg-background/95">
				<div className="mx-auto flex w-full max-w-7xl flex-col gap-5 px-4 py-4 sm:px-6 lg:px-8">
					<div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
						<div className="flex min-w-0 items-center gap-3">
							<picture className="shrink-0">
								<source
									srcSet="/assets/brand/logo.svg"
									type="image/svg+xml"
								/>
								<img
									alt=""
									className="size-11 rounded-md"
									src="/assets/brand/logo.png"
								/>
							</picture>
							<div className="min-w-0">
								<div className="font-semibold text-lg">{t("appName")}</div>
								<div className="truncate text-muted-foreground text-sm">exchange.magical.md</div>
							</div>
						</div>

						<div className="grid min-w-0 gap-3 sm:grid-cols-3 lg:w-130">
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
								onValueChange={(value) => {
									if (isUiLocale(value)) {
										onLocaleChange(value);
									}
								}}
								options={localeOptions}
								value={locale}
							/>
						</div>
					</div>

					<div className="grid gap-3 md:grid-cols-[minmax(0,1fr)_auto] md:items-center">
						<nav className="grid grid-cols-3 gap-1 rounded-md border bg-muted/35 p-1 md:flex md:border-0 md:bg-transparent md:p-0">
							<NavButton
								active={route === "dashboard"}
								icon={<Coins className="size-4" />}
								label={t("currentRates")}
								onClick={() => onNavigate("dashboard")}
							/>
							<NavButton
								active={route === "rates"}
								icon={<Table2 className="size-4" />}
								label={t("allRates")}
								onClick={() => onNavigate("rates")}
							/>
							<NavButton
								active={route === "history"}
								icon={<BarChart3 className="size-4" />}
								label={t("history")}
								onClick={() => onNavigate("history")}
							/>
						</nav>
						<div className="grid grid-cols-2 gap-2 sm:flex sm:flex-wrap md:justify-end">
							<Button
								aria-label={theme === "dark" ? t("lightMode") : t("darkMode")}
								className="min-w-0 shrink whitespace-normal"
								onClick={onThemeChange}
								type="button"
								variant="outline"
							>
								{theme === "dark" ? <Sun className="size-4" /> : <Moon className="size-4" />}
								<span className="truncate">{theme === "dark" ? t("lightMode") : t("darkMode")}</span>
							</Button>
							<Button
								className="min-w-0 shrink whitespace-normal"
								onClick={onRefresh}
								type="button"
								variant="outline"
							>
								<RefreshCw className="size-4" />
								<span className="truncate">{t("refresh")}</span>
							</Button>
						</div>
					</div>
				</div>
			</header>

			<main className="mx-auto w-full max-w-7xl overflow-hidden px-4 py-6 sm:px-6 lg:px-8">{children}</main>
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
			className={cn("min-w-0 shrink whitespace-normal px-2 leading-tight sm:px-4", !active && "text-muted-foreground")}
			onClick={onClick}
			type="button"
			variant={active ? "default" : "ghost"}
		>
			{icon}
			<span className="truncate">{label}</span>
		</Button>
	);
}
