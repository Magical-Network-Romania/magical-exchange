package net.magical.exchange.desktop.model;

import java.time.LocalDate;
import java.util.List;

public record OfficialRatesDto(String country, String baseCurrency, LocalDate rateDate, List<OfficialRateDto> rates) {

	public OfficialRatesDto {
		rates = List.copyOf(rates);
	}
}
