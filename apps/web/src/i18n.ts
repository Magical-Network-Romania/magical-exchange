export const supportedLocales = ["ro", "en", "ru"] as const;

export type UiLocale = (typeof supportedLocales)[number];

const dictionaries = {
	en: {
		address: "Address",
		amount: "Amount",
		appName: "Magical Exchange",
		baseCurrency: "Base currency",
		bestOffers: "Best offers",
		buy: "Buy",
		buyForeign: "Buy foreign currency",
		cardOfficialDescription: "National bank reference rates for the selected country.",
		cardOfficialTitle: "Official rates",
		city: "City",
		close: "Close",
		converter: "Official converter",
		country: "Country",
		currency: "Currency",
		currentRates: "Current rates",
		dashboardSubtitle: "Official rates, local branch prices, and office details for the selected city.",
		dashboardTitle: "Exchange rates in your city",
		details: "Details",
		email: "Email",
		emptyBestRates: "No matching local offers were found yet.",
		emptyHistory: "No official history points were found for this range.",
		emptyLocations: "No active locations are configured for this city.",
		emptyMarketRates: "No city market rates have been fetched yet.",
		emptyOfficialRates: "No official rates have been fetched yet.",
		fetched: "Fetched",
		from: "From",
		history: "History",
		historySubtitle: "Official national-bank history for the selected currency.",
		historyTitle: "Official rate history",
		institution: "Institution",
		language: "Language",
		latestOfficialDate: "Official date",
		loading: "Loading",
		locationDetails: "Location details",
		locations: "Locations",
		marketRates: "Market rates",
		noConverterRate: "Choose a currency with an official rate to use the converter.",
		office: "Office",
		officialRate: "Official rate",
		operation: "Operation",
		phone: "Phone",
		published: "Published",
		rate: "Rate",
		refresh: "Refresh",
		retry: "Retry",
		sell: "Sell",
		sellForeign: "Sell foreign currency",
		statusNoData: "Waiting for source data",
		tableHistory: "History points",
		to: "To",
		unit: "Unit",
		website: "Website"
	},
	ro: {
		address: "Adresă",
		amount: "Sumă",
		appName: "Magical Exchange",
		baseCurrency: "Monedă de bază",
		bestOffers: "Cele mai bune oferte",
		buy: "Cumpărare",
		buyForeign: "Cumpăr valută",
		cardOfficialDescription: "Cursurile de referință ale băncii naționale pentru țara selectată.",
		cardOfficialTitle: "Curs oficial",
		city: "Oraș",
		close: "Închide",
		converter: "Convertor oficial",
		country: "Țară",
		currency: "Valută",
		currentRates: "Cursuri curente",
		dashboardSubtitle: "Curs oficial, prețuri locale și detalii despre oficii pentru orașul selectat.",
		dashboardTitle: "Curs valutar în orașul tău",
		details: "Detalii",
		email: "Email",
		emptyBestRates: "Nu există încă oferte locale pentru această selecție.",
		emptyHistory: "Nu există puncte istorice pentru acest interval.",
		emptyLocations: "Nu există locații active configurate pentru acest oraș.",
		emptyMarketRates: "Cursurile locale pentru oraș nu au fost preluate încă.",
		emptyOfficialRates: "Cursurile oficiale nu au fost preluate încă.",
		fetched: "Preluat",
		from: "De la",
		history: "Istoric",
		historySubtitle: "Istoricul oficial al băncii naționale pentru valuta selectată.",
		historyTitle: "Istoric curs oficial",
		institution: "Instituție",
		language: "Limbă",
		latestOfficialDate: "Data oficială",
		loading: "Se încarcă",
		locationDetails: "Detalii locație",
		locations: "Locații",
		marketRates: "Cursuri comerciale",
		noConverterRate: "Alege o valută cu curs oficial pentru convertor.",
		office: "Oficiu",
		officialRate: "Curs oficial",
		operation: "Operațiune",
		phone: "Telefon",
		published: "Publicat",
		rate: "Curs",
		refresh: "Actualizează",
		retry: "Reîncearcă",
		sell: "Vânzare",
		sellForeign: "Vând valută",
		statusNoData: "Așteptăm datele sursă",
		tableHistory: "Puncte istorice",
		to: "Până la",
		unit: "Unitate",
		website: "Website"
	},
	ru: {
		address: "Адрес",
		amount: "Сумма",
		appName: "Magical Exchange",
		baseCurrency: "Базовая валюта",
		bestOffers: "Лучшие предложения",
		buy: "Покупка",
		buyForeign: "Купить валюту",
		cardOfficialDescription: "Справочные курсы национального банка для выбранной страны.",
		cardOfficialTitle: "Официальный курс",
		city: "Город",
		close: "Закрыть",
		converter: "Официальный конвертер",
		country: "Страна",
		currency: "Валюта",
		currentRates: "Текущие курсы",
		dashboardSubtitle: "Официальный курс, местные цены и данные офисов для выбранного города.",
		dashboardTitle: "Курсы валют в вашем городе",
		details: "Детали",
		email: "Email",
		emptyBestRates: "Подходящих местных предложений пока нет.",
		emptyHistory: "За этот период нет официальных исторических данных.",
		emptyLocations: "Для этого города пока нет активных локаций.",
		emptyMarketRates: "Местные курсы для города еще не загружены.",
		emptyOfficialRates: "Официальные курсы еще не загружены.",
		fetched: "Получено",
		from: "С",
		history: "История",
		historySubtitle: "Официальная история национального банка по выбранной валюте.",
		historyTitle: "История официального курса",
		institution: "Учреждение",
		language: "Язык",
		latestOfficialDate: "Официальная дата",
		loading: "Загрузка",
		locationDetails: "Данные локации",
		locations: "Локации",
		marketRates: "Коммерческие курсы",
		noConverterRate: "Выберите валюту с официальным курсом для конвертера.",
		office: "Офис",
		officialRate: "Официальный курс",
		operation: "Операция",
		phone: "Телефон",
		published: "Опубликовано",
		rate: "Курс",
		refresh: "Обновить",
		retry: "Повторить",
		sell: "Продажа",
		sellForeign: "Продать валюту",
		statusNoData: "Ожидаем данные источника",
		tableHistory: "Исторические точки",
		to: "По",
		unit: "Единица",
		website: "Сайт"
	}
} as const;

export type TranslationKey = keyof typeof dictionaries.en;

export const localeLabels: Record<UiLocale, string> = {
	en: "English",
	ro: "Română",
	ru: "Русский"
};

export function isUiLocale(locale: string): locale is UiLocale {
	return supportedLocales.includes(locale as UiLocale);
}

export function detectUiLocale() {
	const storedLocale = window.localStorage.getItem("magical-exchange.locale");

	if (storedLocale && isUiLocale(storedLocale)) {
		return storedLocale;
	}

	for (const language of window.navigator.languages) {
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
