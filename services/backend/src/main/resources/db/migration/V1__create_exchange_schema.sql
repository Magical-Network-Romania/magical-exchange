CREATE TABLE locales (
	code VARCHAR(12) PRIMARY KEY,
	language_code CHAR(2) NOT NULL,
	region_code CHAR(2),
	native_name VARCHAR(80) NOT NULL,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT ck_locales_code_format CHECK (code ~ '^[a-z]{2}(-[A-Z]{2})?$'),
	CONSTRAINT ck_locales_language_code_format CHECK (language_code ~ '^[a-z]{2}$'),
	CONSTRAINT ck_locales_region_code_format CHECK (region_code IS NULL OR region_code ~ '^[A-Z]{2}$')
);

CREATE TABLE currencies (
	code CHAR(3) PRIMARY KEY,
	numeric_code CHAR(3) UNIQUE,
	minor_units SMALLINT NOT NULL DEFAULT 2,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT ck_currencies_code_format CHECK (code ~ '^[A-Z]{3}$'),
	CONSTRAINT ck_currencies_numeric_code_format CHECK (numeric_code IS NULL OR numeric_code ~ '^[0-9]{3}$')
);

CREATE TABLE currency_translations (
	currency_code CHAR(3) NOT NULL REFERENCES currencies (code) ON DELETE CASCADE,
	locale_code VARCHAR(12) NOT NULL REFERENCES locales (code),
	name VARCHAR(120) NOT NULL,
	PRIMARY KEY (currency_code, locale_code)
);

CREATE TABLE countries (
	code CHAR(2) PRIMARY KEY,
	base_currency_code CHAR(3) NOT NULL REFERENCES currencies (code),
	default_locale_code VARCHAR(12) NOT NULL REFERENCES locales (code),
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT ck_countries_code_format CHECK (code ~ '^[A-Z]{2}$')
);

CREATE TABLE country_translations (
	country_code CHAR(2) NOT NULL REFERENCES countries (code) ON DELETE CASCADE,
	locale_code VARCHAR(12) NOT NULL REFERENCES locales (code),
	name VARCHAR(120) NOT NULL,
	PRIMARY KEY (country_code, locale_code)
);

CREATE TABLE cities (
	country_code CHAR(2) NOT NULL REFERENCES countries (code),
	slug VARCHAR(80) NOT NULL,
	timezone VARCHAR(80) NOT NULL,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	PRIMARY KEY (country_code, slug)
);

CREATE TABLE city_translations (
	country_code CHAR(2) NOT NULL,
	city_slug VARCHAR(80) NOT NULL,
	locale_code VARCHAR(12) NOT NULL REFERENCES locales (code),
	name VARCHAR(120) NOT NULL,
	PRIMARY KEY (country_code, city_slug, locale_code),
	CONSTRAINT fk_city_translations_city FOREIGN KEY (country_code, city_slug) REFERENCES cities (country_code, slug) ON DELETE CASCADE
);

CREATE TABLE institutions (
	id UUID PRIMARY KEY DEFAULT uuidv7(),
	country_code CHAR(2) NOT NULL REFERENCES countries (code),
	slug VARCHAR(100) NOT NULL,
	name VARCHAR(160) NOT NULL,
	institution_type VARCHAR(40) NOT NULL,
	website_url TEXT,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT ck_institutions_type CHECK (institution_type IN ('NATIONAL_BANK', 'BANK', 'EXCHANGE_OFFICE')),
	CONSTRAINT uq_institutions_country_slug UNIQUE (country_code, slug),
	CONSTRAINT uq_institutions_id_country UNIQUE (id, country_code)
);

CREATE TABLE locations (
	id UUID PRIMARY KEY DEFAULT uuidv7(),
	institution_id UUID NOT NULL,
	country_code CHAR(2) NOT NULL,
	city_slug VARCHAR(80) NOT NULL,
	slug VARCHAR(120) NOT NULL,
	name VARCHAR(180) NOT NULL,
	address TEXT NOT NULL,
	latitude NUMERIC(9, 6),
	longitude NUMERIC(9, 6),
	phone TEXT,
	email TEXT,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT uq_locations_city_slug UNIQUE (country_code, city_slug, slug),
	CONSTRAINT uq_locations_id_country_city UNIQUE (id, country_code, city_slug),
	CONSTRAINT fk_locations_city FOREIGN KEY (country_code, city_slug) REFERENCES cities (country_code, slug),
	CONSTRAINT fk_locations_institution_country FOREIGN KEY (institution_id, country_code) REFERENCES institutions (id, country_code)
);

CREATE TABLE rate_sources (
	id UUID PRIMARY KEY DEFAULT uuidv7(),
	institution_id UUID NOT NULL,
	location_id UUID,
	country_code CHAR(2) NOT NULL REFERENCES countries (code),
	city_slug VARCHAR(80),
	slug VARCHAR(120) NOT NULL UNIQUE,
	source_type VARCHAR(40) NOT NULL,
	parser_key VARCHAR(80) NOT NULL,
	source_url TEXT NOT NULL,
	refresh_interval_minutes INTEGER NOT NULL,
	enabled BOOLEAN NOT NULL DEFAULT TRUE,
	config_json JSONB NOT NULL DEFAULT '{}'::JSONB,
	last_fetched_at TIMESTAMPTZ,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT ck_rate_sources_type CHECK (source_type IN ('OFFICIAL_XML', 'MARKET_HTML')),
	CONSTRAINT ck_rate_sources_refresh_positive CHECK (refresh_interval_minutes > 0),
	CONSTRAINT fk_rate_sources_city FOREIGN KEY (country_code, city_slug) REFERENCES cities (country_code, slug),
	CONSTRAINT fk_rate_sources_institution_country FOREIGN KEY (institution_id, country_code) REFERENCES institutions (id, country_code),
	CONSTRAINT fk_rate_sources_location_country_city FOREIGN KEY (location_id, country_code, city_slug) REFERENCES locations (id, country_code, city_slug)
);

CREATE TABLE official_exchange_rates (
	id UUID PRIMARY KEY DEFAULT uuidv7(),
	country_code CHAR(2) NOT NULL REFERENCES countries (code),
	currency_code CHAR(3) NOT NULL REFERENCES currencies (code),
	rate_date DATE NOT NULL,
	unit INTEGER NOT NULL DEFAULT 1,
	rate NUMERIC(20, 8) NOT NULL,
	source_id UUID REFERENCES rate_sources (id),
	fetched_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT ck_official_exchange_rates_unit_positive CHECK (unit > 0),
	CONSTRAINT ck_official_exchange_rates_rate_positive CHECK (rate > 0),
	CONSTRAINT uq_official_exchange_rates UNIQUE (country_code, currency_code, rate_date)
);

CREATE TABLE market_rate_batches (
	id UUID PRIMARY KEY DEFAULT uuidv7(),
	source_id UUID NOT NULL REFERENCES rate_sources (id),
	institution_id UUID NOT NULL,
	location_id UUID,
	country_code CHAR(2) NOT NULL REFERENCES countries (code),
	city_slug VARCHAR(80),
	source_published_at TIMESTAMPTZ,
	fetched_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	checksum CHAR(64) NOT NULL,
	CONSTRAINT uq_market_rate_batches_source_checksum UNIQUE (source_id, checksum),
	CONSTRAINT fk_market_rate_batches_city FOREIGN KEY (country_code, city_slug) REFERENCES cities (country_code, slug),
	CONSTRAINT fk_market_rate_batches_institution_country FOREIGN KEY (institution_id, country_code) REFERENCES institutions (id, country_code),
	CONSTRAINT fk_market_rate_batches_location_country_city FOREIGN KEY (location_id, country_code, city_slug) REFERENCES locations (id, country_code, city_slug)
);

CREATE TABLE market_exchange_rates (
	id UUID PRIMARY KEY DEFAULT uuidv7(),
	batch_id UUID NOT NULL REFERENCES market_rate_batches (id) ON DELETE CASCADE,
	currency_code CHAR(3) NOT NULL REFERENCES currencies (code),
	rate_type VARCHAR(40) NOT NULL,
	buy_rate NUMERIC(20, 8),
	sell_rate NUMERIC(20, 8),
	official_rate NUMERIC(20, 8),
	unit INTEGER NOT NULL DEFAULT 1,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT ck_market_exchange_rates_type CHECK (rate_type IN ('CASH', 'CARD', 'ATM')),
	CONSTRAINT ck_market_exchange_rates_unit_positive CHECK (unit > 0),
	CONSTRAINT ck_market_exchange_rates_has_rate CHECK (buy_rate IS NOT NULL OR sell_rate IS NOT NULL),
	CONSTRAINT uq_market_exchange_rates UNIQUE (batch_id, currency_code, rate_type)
);

CREATE TABLE source_fetch_runs (
	id UUID PRIMARY KEY DEFAULT uuidv7(),
	source_id UUID NOT NULL REFERENCES rate_sources (id),
	status VARCHAR(40) NOT NULL,
	started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	finished_at TIMESTAMPTZ,
	items_upserted INTEGER NOT NULL DEFAULT 0,
	error_message TEXT,
	CONSTRAINT ck_source_fetch_runs_status CHECK (status IN ('STARTED', 'SUCCESS', 'FAILED', 'SKIPPED'))
);

CREATE INDEX ix_cities_country ON cities (country_code);
CREATE INDEX ix_city_translations_locale ON city_translations (locale_code);
CREATE INDEX ix_country_translations_locale ON country_translations (locale_code);
CREATE INDEX ix_currency_translations_locale ON currency_translations (locale_code);
CREATE INDEX ix_locales_active ON locales (active);
CREATE INDEX ix_locations_city ON locations (country_code, city_slug);
CREATE INDEX ix_locations_institution ON locations (institution_id);
CREATE INDEX ix_rate_sources_enabled ON rate_sources (enabled);
CREATE INDEX ix_official_exchange_rates_lookup ON official_exchange_rates (country_code, currency_code, rate_date DESC);
CREATE INDEX ix_market_rate_batches_lookup ON market_rate_batches (source_id, fetched_at DESC);
CREATE INDEX ix_market_exchange_rates_currency ON market_exchange_rates (currency_code, rate_type);
CREATE INDEX ix_source_fetch_runs_source_started ON source_fetch_runs (source_id, started_at DESC);
