import type { TranslationKey } from "@/i18n";

export type Translate = (key: TranslationKey) => string;

export type SelectOption = {
	label: string;
	value: string;
};
