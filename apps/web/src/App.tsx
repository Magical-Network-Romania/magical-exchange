import { useCallback, useEffect, useState } from "react";

import {
	type BootstrapDto,
	type CityDto,
	type CountryDto,
	fetchBootstrap,
	fetchCities,
	fetchCountries,
	isAbortError
} from "@/api/exchange";
import { type AppRoute, AppShell, type ThemeMode } from "@/components/exchange/app-shell";
import { DashboardPage } from "@/components/exchange/dashboard-page";
import { HistoryPage } from "@/components/exchange/history-page";
import { MarketRatesPage } from "@/components/exchange/market-rates-page";
import { detectUiLocale, persistUiLocale, type TranslationKey, translate, type UiLocale } from "@/i18n";

const defaultCountry = "MD";
const defaultCity = "chisinau";
const defaultCurrency = "EUR";

function routeFromPathname(pathname: string): AppRoute {
	if (pathname === "/history") {
		return "history";
	}

	if (pathname === "/rates") {
		return "rates";
	}

	return "dashboard";
}

function pathnameForRoute(route: AppRoute) {
	return {
		dashboard: "/",
		history: "/history",
		rates: "/rates"
	}[route];
}

function getStoredValue(key: string, fallback: string) {
	return window.localStorage.getItem(key) ?? fallback;
}

function detectTheme(): ThemeMode {
	const storedTheme = window.localStorage.getItem("magical-exchange.theme");

	if (storedTheme === "dark" || storedTheme === "light") {
		return storedTheme;
	}

	return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

export function App() {
	const [locale, setLocale] = useState<UiLocale>(() => detectUiLocale());
	const [route, setRoute] = useState<AppRoute>(() => routeFromPathname(window.location.pathname));
	const [theme, setTheme] = useState(() => detectTheme());
	const [country, setCountry] = useState(() => getStoredValue("magical-exchange.country", defaultCountry));
	const [city, setCity] = useState(() => getStoredValue("magical-exchange.city", defaultCity));
	const [selectedCurrency, setSelectedCurrency] = useState(() => getStoredValue("magical-exchange.currency", defaultCurrency));
	const [refreshToken, setRefreshToken] = useState(0);
	const [countries, setCountries] = useState<CountryDto[]>([]);
	const [cities, setCities] = useState<CityDto[]>([]);
	const [bootstrap, setBootstrap] = useState<BootstrapDto | null>(null);
	const [isBootstrapLoading, setIsBootstrapLoading] = useState(false);
	const [bootstrapError, setBootstrapError] = useState<string | null>(null);
	const t = useCallback((key: TranslationKey) => translate(locale, key), [locale]);

	useEffect(() => {
		function handlePopState() {
			setRoute(routeFromPathname(window.location.pathname));
		}

		window.addEventListener("popstate", handlePopState);

		return () => window.removeEventListener("popstate", handlePopState);
	}, []);

	useEffect(() => {
		persistUiLocale(locale);
	}, [locale]);

	useEffect(() => {
		document.documentElement.classList.toggle("dark", theme === "dark");
		window.localStorage.setItem("magical-exchange.theme", theme);
	}, [theme]);

	useEffect(() => {
		window.localStorage.setItem("magical-exchange.country", country);
	}, [country]);

	useEffect(() => {
		window.localStorage.setItem("magical-exchange.city", city);
	}, [city]);

	useEffect(() => {
		window.localStorage.setItem("magical-exchange.currency", selectedCurrency);
	}, [selectedCurrency]);

	useEffect(() => {
		const controller = new AbortController();
		void refreshToken;

		fetchCountries(locale, { signal: controller.signal })
			.then(setCountries)
			.catch((caughtError: unknown) => {
				if (!isAbortError(caughtError)) {
					setCountries([]);
				}
			});

		return () => controller.abort();
	}, [locale, refreshToken]);

	useEffect(() => {
		const controller = new AbortController();
		void refreshToken;

		fetchCities(country, locale, { signal: controller.signal })
			.then((nextCities) => {
				setCities(nextCities);
				setCity((currentCity) => {
					if (nextCities.some((item) => item.slug === currentCity)) {
						return currentCity;
					}

					return nextCities[0]?.slug ?? currentCity;
				});
			})
			.catch((caughtError: unknown) => {
				if (!isAbortError(caughtError)) {
					setCities([]);
				}
			});

		return () => controller.abort();
	}, [country, locale, refreshToken]);

	useEffect(() => {
		const controller = new AbortController();
		void refreshToken;

		setIsBootstrapLoading(true);
		setBootstrapError(null);
		setBootstrap(null);

		fetchBootstrap(country, city, locale, { signal: controller.signal })
			.then((nextBootstrap) => {
				setBootstrap(nextBootstrap);
				setIsBootstrapLoading(false);
			})
			.catch((caughtError: unknown) => {
				if (!isAbortError(caughtError)) {
					setBootstrapError(caughtError instanceof Error ? caughtError.message : "Unknown API error");
					setBootstrap(null);
					setIsBootstrapLoading(false);
				}
			});

		return () => controller.abort();
	}, [city, country, locale, refreshToken]);

	useEffect(() => {
		if (!bootstrap) {
			return;
		}

		const foreignCurrencies = bootstrap.currencies.filter((currency) => currency.code !== bootstrap.country.baseCurrencyCode);

		if (foreignCurrencies.length > 0 && !foreignCurrencies.some((currency) => currency.code === selectedCurrency)) {
			setSelectedCurrency(foreignCurrencies[0]?.code ?? defaultCurrency);
		}
	}, [bootstrap, selectedCurrency]);

	function navigate(nextRoute: AppRoute) {
		const nextPathname = pathnameForRoute(nextRoute);

		if (window.location.pathname !== nextPathname) {
			window.history.pushState(null, "", nextPathname);
		}

		setRoute(nextRoute);
	}

	function refresh() {
		setRefreshToken((currentToken) => currentToken + 1);
	}

	return (
		<AppShell
			cities={cities}
			city={city}
			countries={countries}
			country={country}
			locale={locale}
			onCityChange={setCity}
			onCountryChange={setCountry}
			onLocaleChange={setLocale}
			onNavigate={navigate}
			onRefresh={refresh}
			onThemeChange={() => setTheme((currentTheme) => (currentTheme === "dark" ? "light" : "dark"))}
			route={route}
			t={t}
			theme={theme}
		>
			{(() => {
				switch (route) {
					case "dashboard": {
						return (
							<DashboardPage
								bootstrap={bootstrap}
								error={bootstrapError}
								isLoading={isBootstrapLoading}
								locale={locale}
								onRefresh={refresh}
								onSelectedCurrencyChange={setSelectedCurrency}
								selectedCurrency={selectedCurrency}
								t={t}
							/>
						);
					}

					case "rates": {
						return (
							<MarketRatesPage
								bootstrap={bootstrap}
								error={bootstrapError}
								isLoading={isBootstrapLoading}
								locale={locale}
								onRefresh={refresh}
								t={t}
							/>
						);
					}

					case "history": {
						return (
							<HistoryPage
								bootstrap={bootstrap}
								country={country}
								locale={locale}
								onSelectedCurrencyChange={setSelectedCurrency}
								selectedCurrency={selectedCurrency}
								t={t}
							/>
						);
					}

					default: {
						return null;
					}
				}
			})()}
		</AppShell>
	);
}
