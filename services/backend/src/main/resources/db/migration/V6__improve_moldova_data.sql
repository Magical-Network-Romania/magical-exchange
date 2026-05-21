UPDATE rate_sources
SET refresh_interval_minutes = 60,
	updated_at = NOW()
WHERE slug = 'bnm-official-md'
	AND source_type = 'OFFICIAL_XML'
	AND parser_key = 'bnm-official-xml';

UPDATE rate_sources
SET source_url = 'https://www.victoriabank.md/curs-valutar',
	updated_at = NOW()
WHERE slug = 'victoriabank-md-cash'
	AND source_type = 'MARKET_HTML'
	AND parser_key = 'victoriabank-html';
