import type { ThemeMode } from "@/components/app-shell";

export const defaultCountry = "MD";
export const defaultCity = "chisinau";
export const defaultCurrency = "EUR";

export function getStoredValue(key: string, fallback: string) {
	return window.localStorage.getItem(key) ?? fallback;
}

export function detectTheme(): ThemeMode {
	const storedTheme = window.localStorage.getItem("magical-exchange.theme");

	if (storedTheme === "dark" || storedTheme === "light") {
		return storedTheme;
	}

	return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}
