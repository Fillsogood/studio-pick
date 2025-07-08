package org.example.studiopick.application.studio.dto;

import java.math.BigDecimal;

public record PricingDto(
    BigDecimal weekdayPrice,
    BigDecimal weekendPrice
) {}
