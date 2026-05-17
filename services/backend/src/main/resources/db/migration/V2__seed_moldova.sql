INSERT INTO locales (code, language_code, region_code, native_name)
VALUES
	('en', 'en', NULL, 'English'),
	('ro', 'ro', NULL, 'Română'),
	('ru', 'ru', NULL, 'Русский')
ON CONFLICT (code) DO UPDATE SET
	language_code = EXCLUDED.language_code,
	region_code = EXCLUDED.region_code,
	native_name = EXCLUDED.native_name,
	active = TRUE;

INSERT INTO currencies (code, numeric_code, minor_units)
VALUES
	('MDL', '498', 2),
	('EUR', '978', 2),
	('USD', '840', 2),
	('RON', '946', 2),
	('UAH', '980', 2),
	('GBP', '826', 2),
	('CHF', '756', 2),
	('RUB', '643', 2)
ON CONFLICT (code) DO UPDATE SET
	numeric_code = EXCLUDED.numeric_code,
	minor_units = EXCLUDED.minor_units,
	active = TRUE;

INSERT INTO currency_translations (currency_code, locale_code, name)
VALUES
	('MDL', 'en', 'Moldovan leu'),
	('MDL', 'ro', 'Leu moldovenesc'),
	('MDL', 'ru', 'Молдавский лей'),
	('EUR', 'en', 'Euro'),
	('EUR', 'ro', 'Euro'),
	('EUR', 'ru', 'Евро'),
	('USD', 'en', 'US dollar'),
	('USD', 'ro', 'Dolar american'),
	('USD', 'ru', 'Доллар США'),
	('RON', 'en', 'Romanian leu'),
	('RON', 'ro', 'Leu românesc'),
	('RON', 'ru', 'Румынский лей'),
	('UAH', 'en', 'Ukrainian hryvnia'),
	('UAH', 'ro', 'Grivnă ucraineană'),
	('UAH', 'ru', 'Украинская гривна'),
	('GBP', 'en', 'Pound sterling'),
	('GBP', 'ro', 'Liră sterlină'),
	('GBP', 'ru', 'Фунт стерлингов'),
	('CHF', 'en', 'Swiss franc'),
	('CHF', 'ro', 'Franc elvețian'),
	('CHF', 'ru', 'Швейцарский франк'),
	('RUB', 'en', 'Russian ruble'),
	('RUB', 'ro', 'Rublă rusească'),
	('RUB', 'ru', 'Российский рубль')
ON CONFLICT (currency_code, locale_code) DO UPDATE SET
	name = EXCLUDED.name;

INSERT INTO countries (code, base_currency_code, default_locale_code)
VALUES ('MD', 'MDL', 'ro')
ON CONFLICT (code) DO UPDATE SET
	base_currency_code = EXCLUDED.base_currency_code,
	default_locale_code = EXCLUDED.default_locale_code,
	active = TRUE;

INSERT INTO country_translations (country_code, locale_code, name)
VALUES
	('MD', 'en', 'Moldova'),
	('MD', 'ro', 'Republica Moldova'),
	('MD', 'ru', 'Молдова')
ON CONFLICT (country_code, locale_code) DO UPDATE SET
	name = EXCLUDED.name;

INSERT INTO cities (country_code, slug, timezone)
VALUES
	('MD', 'chisinau', 'Europe/Chisinau'),
	('MD', 'balti', 'Europe/Chisinau')
ON CONFLICT (country_code, slug) DO UPDATE SET
	timezone = EXCLUDED.timezone,
	active = TRUE;

INSERT INTO city_translations (country_code, city_slug, locale_code, name)
VALUES
	('MD', 'chisinau', 'en', 'Chișinău'),
	('MD', 'chisinau', 'ro', 'Chișinău'),
	('MD', 'chisinau', 'ru', 'Кишинёв'),
	('MD', 'balti', 'en', 'Bălți'),
	('MD', 'balti', 'ro', 'Bălți'),
	('MD', 'balti', 'ru', 'Бельцы')
ON CONFLICT (country_code, city_slug, locale_code) DO UPDATE SET
	name = EXCLUDED.name;

INSERT INTO institutions (country_code, slug, name, institution_type, website_url)
VALUES
	('MD', 'bnm', 'National Bank of Moldova', 'NATIONAL_BANK', 'https://www.bnm.md'),
	('MD', 'maib', 'maib', 'BANK', 'https://www.maib.md'),
	('MD', 'otp-bank-md', 'OTP Bank Moldova', 'BANK', 'https://www.otpbank.md'),
	('MD', 'victoriabank', 'Victoriabank', 'BANK', 'https://www.victoriabank.md'),
	('MD', 'eximbank-md', 'Eximbank Moldova', 'BANK', 'https://eximbank.md')
ON CONFLICT (country_code, slug) DO UPDATE SET
	name = EXCLUDED.name,
	institution_type = EXCLUDED.institution_type,
	website_url = EXCLUDED.website_url,
	active = TRUE;

INSERT INTO locations (institution_id, country_code, city_slug, slug, name, address, phone, email)
SELECT institution.id, 'MD', seed.city_slug, seed.slug, seed.name, seed.address, seed.phone, seed.email
FROM (
	VALUES
		('maib', 'chisinau', 'maib-central-chisinau', 'maib Central Office', 'str. 31 August 1989, 127, Chișinău', '1313', 'info@maib.md'),
		('maib', 'chisinau', 'maib-decebal-chisinau', 'maib Decebal', 'bd. Decebal, Chișinău', '1313', 'info@maib.md'),
		('otp-bank-md', 'chisinau', 'otp-decebal-91-chisinau', 'OTP Bank Decebal', 'bd. Decebal 91, Chișinău', '+373 22 256 456', 'info@otpbank.md'),
		('victoriabank', 'chisinau', 'victoriabank-head-office-chisinau', 'Victoriabank Head Office', 'str. 31 August 1989, 141, Chișinău', '1303', 'office@vb.md'),
		('victoriabank', 'chisinau', 'victoriabank-decebal-99-chisinau', 'Victoriabank Decebal', 'bd. Decebal 99, Chișinău', '1303', 'suport@vb.md'),
		('eximbank-md', 'chisinau', 'eximbank-head-office-chisinau', 'Eximbank Head Office', 'bd. Ștefan cel Mare și Sfânt 171/1, Chișinău', '+373 22 600 000', 'contact@eximbank.com'),
		('eximbank-md', 'chisinau', 'eximbank-central-chisinau', 'Eximbank Central', 'bd. Ștefan cel Mare și Sfânt, Chișinău', '+373 22 600 000', 'contact@eximbank.com'),
		('maib', 'balti', 'maib-stefan-8-1-balti', 'maib Bălți', 'str. Ștefan cel Mare 8/1, Bălți', '1313', 'info@maib.md'),
		('otp-bank-md', 'balti', 'otp-stefan-2-balti', 'OTP Bank Bălți', 'str. Ștefan cel Mare 2, Bălți', '+373 22 256 456', 'info@otpbank.md'),
		('victoriabank', 'balti', 'victoriabank-puskin-18-balti', 'Victoriabank Sucursala nr. 1 Bălți', 'str. A. Pușkin 18, Bălți', '030 303 422', 'suport@vb.md'),
		('eximbank-md', 'balti', 'eximbank-stefan-6-2-balti', 'Eximbank Bălți', 'str. Ștefan cel Mare 6/2, Bălți', '030 301 448', 'contact@eximbank.com')
) AS seed(institution_slug, city_slug, slug, name, address, phone, email)
JOIN institutions institution ON institution.country_code = 'MD' AND institution.slug = seed.institution_slug
ON CONFLICT (country_code, city_slug, slug) DO UPDATE SET
	name = EXCLUDED.name,
	address = EXCLUDED.address,
	phone = EXCLUDED.phone,
	email = EXCLUDED.email,
	active = TRUE;

INSERT INTO rate_sources (institution_id, country_code, slug, source_type, parser_key, source_url, refresh_interval_minutes, config_json)
SELECT institution.id,
	'MD',
	'bnm-official-md',
	'OFFICIAL_XML',
	'bnm-official-xml',
	'https://www.bnm.md/en/official_exchange_rates?get_xml=1&date={date}',
	1440,
	'{"dateFormat":"dd.MM.yyyy"}'::JSONB
FROM institutions institution
WHERE institution.country_code = 'MD'
	AND institution.slug = 'bnm'
ON CONFLICT (slug) DO UPDATE SET
	source_url = EXCLUDED.source_url,
	parser_key = EXCLUDED.parser_key,
	refresh_interval_minutes = EXCLUDED.refresh_interval_minutes,
	config_json = EXCLUDED.config_json,
	enabled = TRUE;

INSERT INTO rate_sources (institution_id, country_code, slug, source_type, parser_key, source_url, refresh_interval_minutes, config_json)
SELECT institution.id, 'MD', seed.slug, 'MARKET_HTML', seed.parser_key, seed.source_url, 60, '{"rateType":"CASH"}'::JSONB
FROM (
	VALUES
		('maib-md-cash', 'maib', 'maib-html', 'https://www.maib.md/en/curs-valutar/'),
		('otp-md-cash', 'otp-bank-md', 'otpbank-md-html', 'https://www.otpbank.md/exchange'),
		('victoriabank-md-cash', 'victoriabank', 'victoriabank-html', 'https://www.victoriabank.md/ru/kurs-obmena'),
		('eximbank-md-cash', 'eximbank-md', 'eximbank-md-html', 'https://eximbank.md/exchange')
) AS seed(slug, institution_slug, parser_key, source_url)
JOIN institutions institution ON institution.country_code = 'MD' AND institution.slug = seed.institution_slug
ON CONFLICT (slug) DO UPDATE SET
	source_url = EXCLUDED.source_url,
	parser_key = EXCLUDED.parser_key,
	refresh_interval_minutes = EXCLUDED.refresh_interval_minutes,
	config_json = EXCLUDED.config_json,
	enabled = TRUE;
