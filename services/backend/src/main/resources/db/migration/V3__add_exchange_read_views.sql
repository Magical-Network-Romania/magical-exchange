CREATE VIEW active_countries AS
SELECT country.code,
	country.base_currency_code,
	country.default_locale_code
FROM countries country
WHERE country.active;

CREATE VIEW active_cities AS
SELECT city.country_code,
	city.slug,
	city.timezone
FROM cities city
JOIN active_countries country ON country.code = city.country_code
WHERE city.active;

CREATE VIEW active_currencies AS
SELECT currency.code,
	currency.numeric_code,
	currency.minor_units
FROM currencies currency
WHERE currency.active;

CREATE VIEW active_locations_with_institution AS
SELECT location.id AS location_id,
	location.country_code,
	location.city_slug,
	location.slug AS location_slug,
	location.name AS location_name,
	location.address,
	location.latitude,
	location.longitude,
	location.phone,
	location.email,
	institution.id AS institution_id,
	institution.slug AS institution_slug,
	institution.name AS institution_name,
	institution.institution_type,
	institution.website_url
FROM locations location
JOIN active_cities city
	ON city.country_code = location.country_code
	AND city.slug = location.city_slug
JOIN institutions institution ON institution.id = location.institution_id
WHERE location.active
	AND institution.active;

CREATE VIEW latest_market_rates_by_location AS
SELECT DISTINCT ON (location.location_id, rate.currency_code, rate.rate_type)
	location.location_id,
	location.country_code,
	location.city_slug,
	location.location_slug,
	location.location_name,
	location.address,
	location.latitude,
	location.longitude,
	location.phone,
	location.email,
	location.institution_id,
	location.institution_slug,
	location.institution_name,
	location.institution_type,
	location.website_url,
	batch.id AS batch_id,
	batch.source_id,
	batch.fetched_at,
	batch.source_published_at,
	rate.currency_code,
	rate.rate_type,
	rate.buy_rate,
	rate.sell_rate,
	rate.official_rate,
	rate.unit
FROM market_rate_batches batch
JOIN active_locations_with_institution location
	ON location.institution_id = batch.institution_id
	AND location.country_code = batch.country_code
	AND (batch.city_slug IS NULL OR batch.city_slug = location.city_slug)
	AND (batch.location_id IS NULL OR batch.location_id = location.location_id)
JOIN market_exchange_rates rate ON rate.batch_id = batch.id
ORDER BY location.location_id,
	rate.currency_code,
	rate.rate_type,
	batch.fetched_at DESC,
	batch.id DESC;

CREATE VIEW official_rates_with_source AS
SELECT official_rate.country_code,
	official_rate.currency_code,
	official_rate.rate_date,
	official_rate.unit,
	official_rate.rate,
	official_rate.fetched_at,
	source.slug AS source_slug
FROM official_exchange_rates official_rate
LEFT JOIN rate_sources source ON source.id = official_rate.source_id;

CREATE VIEW due_rate_sources AS
SELECT source.id,
	source.institution_id,
	source.location_id,
	source.country_code,
	source.city_slug,
	source.slug,
	source.source_type,
	source.parser_key,
	source.source_url,
	COALESCE(source.config_json ->> 'rateType', 'CASH') AS rate_type
FROM rate_sources source
WHERE source.enabled
	AND (
		source.last_fetched_at IS NULL
		OR source.last_fetched_at <= NOW() - source.refresh_interval_minutes * INTERVAL '1 minute'
	);
