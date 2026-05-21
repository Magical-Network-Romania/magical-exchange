package net.magical.exchange.desktop.model;

import java.util.List;

public record MarketRateGroup(LocationDto location, List<MarketRateDto> rates) {

	public MarketRateGroup {
		rates = List.copyOf(rates);
	}
}
