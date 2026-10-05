package com.company.MakeMyTrip.pricing_service.domain;

public interface PricingStrategy {

    StrategyResult apply(PricingContext context);

    String getRuleName();
}