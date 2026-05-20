import type { TranslationKey } from "./translations/en";
import { en, type TranslationDictionary } from "./translations/en";
import { ro } from "./translations/ro";
import { ru } from "./translations/ru";

export type { TranslationKey } from "./translations/en";

export const supportedLocales = ["ro", "en", "ru"] as const;

export type UiLocale = (typeof supportedLocales)[number];

export type LocaleOption = {
	label: string;
	value: UiLocale;
};

const dictionaries: Record<UiLocale, TranslationDictionary> = {
	en,
	ro,
	ru
};

export const localeLabels: Record<UiLocale, string> = {
	en: "English",
	ro: "Română",
	ru: "Русский"
};

export const localeOptions = supportedLocales.map((locale) => ({
	label: localeLabels[locale],
	value: locale
})) satisfies LocaleOption[];

export function isUiLocale(locale: string): locale is UiLocale {
	return supportedLocales.includes(locale as UiLocale);
}

export function detectUiLocale() {
	const storedLocale = window.localStorage.getItem("magical-exchange.locale");

	if (storedLocale && isUiLocale(storedLocale)) {
		return storedLocale;
	}

	const browserLanguages = window.navigator.languages.length > 0 ? window.navigator.languages : [window.navigator.language];

	for (const language of browserLanguages) {
		const candidate = language.slice(0, 2).toLowerCase();

		if (isUiLocale(candidate)) {
			return candidate;
		}
	}

	return "en";
}

export function persistUiLocale(locale: UiLocale) {
	window.localStorage.setItem("magical-exchange.locale", locale);
}

export function translate(locale: UiLocale, key: TranslationKey) {
	return dictionaries[locale][key];
}

export function toIntlLocale(locale: UiLocale) {
	return {
		en: "en-US",
		ro: "ro-MD",
		ru: "ru-RU"
	}[locale];
}
