package org.example.studiopick.application.studio.dto;

import java.math.BigDecimal;

public record PricingDto(
    Long hourlyBaseRate,
    BigDecimal weekendPrice,
    Long perPersonRate
) {}
