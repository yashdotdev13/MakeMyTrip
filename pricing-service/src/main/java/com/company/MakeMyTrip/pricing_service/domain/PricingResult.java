package com.company.MakeMyTrip.pricing_service.domain;

import java.math.BigDecimal;
import java.util.List;

public record PricingResult(
        BigDecimal finalPrice,
        List<String> appliedRules
) {
}
