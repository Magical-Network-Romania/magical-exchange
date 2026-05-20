export const supportedLocales = ["ro", "en", "ru"] as const;

export type UiLocale = (typeof supportedLocales)[number];

const dictionaries = {
	en: {
		address: "Address",
		allRates: "All rates",
		allRatesSubtitle: "Every fetched commercial rate grouped by bank branch.",
		allRatesTitle: "All bank exchange rates",
		amount: "Amount",
		appName: "Magical Exchange",
		baseCurrency: "Base currency",
		baseSideLocked: "MDL stays locked on one side",
		bestOffers: "Best offers",
		bestBuyCaption: "Best places to buy",
		bestRate: "Best rate",
		bestSellCaption: "Best places to sell",
		buy: "Buy",
		buyForeign: "Buy foreign currency",
		cardOfficialDescription: "National bank reference rates for the selected country.",
		cardOfficialTitle: "Official rates",
		city: "City",
		close: "Close",
		converter: "Exchange calculator",
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
		lightMode: "Light",
		loading: "Loading",
		locationDetails: "Location details",
		locations: "Locations",
		darkMode: "Dark",
		marketRates: "Market rates",
		noConverterRate: "Choose a currency with an official rate to use the converter.",
		noMarketRatesForCurrency: "No local offers were found for this currency yet.",
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
		website: "Website",
		youPay: "You pay",
		youReceive: "You receive"
	},
	ro: {
		address: "Adresă",
		allRates: "Toate cursurile",
		allRatesSubtitle: "Toate cursurile comerciale preluate, grupate după filială.",
		allRatesTitle: "Toate cursurile băncilor",
		amount: "Sumă",
		appName: "Magical Exchange",
		baseCurrency: "Monedă de bază",
		baseSideLocked: "MDL rămâne fixat pe o parte",
		bestOffers: "Cele mai bune oferte",
		bestBuyCaption: "Cele mai bune locuri unde cumperi",
		bestRate: "Cel mai bun",
		bestSellCaption: "Cele mai bune locuri unde vinzi",
		buy: "Cumpărare",
		buyForeign: "Cumpăr valută",
		cardOfficialDescription: "Cursurile de referință ale băncii naționale pentru țara selectată.",
		cardOfficialTitle: "Curs oficial",
		city: "Oraș",
		close: "Închide",
		converter: "Calculator valutar",
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
		lightMode: "Luminos",
		loading: "Se încarcă",
		locationDetails: "Detalii locație",
		locations: "Locații",
		darkMode: "Întunecat",
		marketRates: "Cursuri comerciale",
		noConverterRate: "Alege o valută cu curs oficial pentru convertor.",
		noMarketRatesForCurrency: "Nu există încă oferte locale pentru această valută.",
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
		website: "Website",
		youPay: "Plătești",
		youReceive: "Primești"
	},
	ru: {
		address: "Адрес",
		allRates: "Все курсы",
		allRatesSubtitle: "Все загруженные коммерческие курсы, сгруппированные по отделениям.",
		allRatesTitle: "Все банковские курсы",
		amount: "Сумма",
		appName: "Magical Exchange",
		baseCurrency: "Базовая валюта",
		baseSideLocked: "MDL закреплен с одной стороны",
		bestOffers: "Лучшие предложения",
		bestBuyCaption: "Лучшие места для покупки",
		bestRate: "Лучший",
		bestSellCaption: "Лучшие места для продажи",
		buy: "Покупка",
		buyForeign: "Купить валюту",
		cardOfficialDescription: "Справочные курсы национального банка для выбранной страны.",
		cardOfficialTitle: "Официальный курс",
		city: "Город",
		close: "Закрыть",
		converter: "Калькулятор обмена",
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
		lightMode: "Светлая",
		loading: "Загрузка",
		locationDetails: "Данные локации",
		locations: "Локации",
		darkMode: "Темная",
		marketRates: "Коммерческие курсы",
		noConverterRate: "Выберите валюту с официальным курсом для конвертера.",
		noMarketRatesForCurrency: "Для этой валюты пока нет местных предложений.",
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
		website: "Сайт",
		youPay: "Платите",
		youReceive: "Получаете"
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
