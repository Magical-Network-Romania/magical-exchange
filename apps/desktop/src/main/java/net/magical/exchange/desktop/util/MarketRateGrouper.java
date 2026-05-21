package net.magical.exchange.desktop.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.magical.exchange.desktop.model.LocationDto;
import net.magical.exchange.desktop.model.MarketRateDto;
import net.magical.exchange.desktop.model.MarketRateGroup;

public final class MarketRateGrouper {

	private MarketRateGrouper() {
	}

	public static List<MarketRateGroup> groupMarketRates(List<MarketRateDto> rates) {
		Map<String, List<MarketRateDto>> ratesByLocation = new LinkedHashMap<>();
		Map<String, LocationDto> locationsById = new LinkedHashMap<>();

		for (MarketRateDto rate : rates) {
			ratesByLocation.computeIfAbsent(rate.location().id(), ignored -> new ArrayList<>()).add(rate);
			locationsById.put(rate.location().id(), rate.location());
		}

		return ratesByLocation.entrySet().stream().map(entry -> toGroup(entry, locationsById.get(entry.getKey())))
				.sorted(Comparator.comparing(group -> group.location().institution().name())).toList();
	}

	private static MarketRateGroup toGroup(Map.Entry<String, List<MarketRateDto>> entry, LocationDto location) {
		List<MarketRateDto> sortedRates = sortRates(entry.getValue());

		return new MarketRateGroup(location, sortedRates);
	}

	private static List<MarketRateDto> sortRates(List<MarketRateDto> rates) {
		return rates.stream().sorted(Comparator.comparing(MarketRateGrouper::currencyCode)).toList();
	}

	private static String currencyCode(MarketRateDto rate) {
		return rate.currency().code();
	}
}
