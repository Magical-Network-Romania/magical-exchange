package net.magical.exchange.desktop.model;

import java.math.BigDecimal;

public record LocationDto(String id, String countryCode, String citySlug, String slug, String name, String address, BigDecimal lat,
		BigDecimal lng, String phone, String email, InstitutionDto institution) {
}
