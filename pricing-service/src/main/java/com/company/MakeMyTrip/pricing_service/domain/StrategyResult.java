package com.company.MakeMyTrip.pricing_service.domain;

import java.math.BigDecimal;
import java.util.List;

public record StrategyResult(BigDecimal price, List<String> appliedRules) {

    public static StrategyResult unchanged(BigDecimal price) {
        return new StrategyResult(price, List.of());
    }

    public static StrategyResult applied(BigDecimal price, String rule) {
        return new StrategyResult(price, List.of(rule));
    }
}