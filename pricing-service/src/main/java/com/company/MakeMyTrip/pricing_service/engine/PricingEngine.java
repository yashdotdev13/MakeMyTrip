package com.company.MakeMyTrip.pricing_service.engine;

import com.company.MakeMyTrip.pricing_service.domain.PricingContext;
import com.company.MakeMyTrip.pricing_service.domain.PricingResult;
import com.company.MakeMyTrip.pricing_service.domain.PricingStrategy;
import com.company.MakeMyTrip.pricing_service.domain.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PricingEngine {

    private final List<PricingStrategy> strategies;

    public PricingEngine(List<PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public PricingResult calculate(PricingContext context) {

        BigDecimal currentPrice = context.basePrice();
        List<String> appliedRules = new ArrayList<>();

        log.debug("Starting pricing calculation: referenceId={}, bookingType={}, basePrice={}",
                context.referenceId(), context.bookingType(), currentPrice);

        for (PricingStrategy strategy : strategies) {
            StrategyResult result = strategy.apply(context.withBasePrice(currentPrice));

            if (result == null) {
                log.warn("Pricing strategy returned null: strategy={}", strategy.getRuleName());
                continue;
            }

            currentPrice = result.price();
            if (result.appliedRules() != null && !result.appliedRules().isEmpty()) {
                appliedRules.addAll(result.appliedRules());
                log.debug("Pricing strategy applied: strategy={}, rules={}",
                        strategy.getRuleName(), result.appliedRules());
            }
        }
        log.debug("Pricing calculation completed: referenceId={}, basePrice={}, finalPrice={}, appliedRules={}",
                context.referenceId(), context.basePrice(), currentPrice, appliedRules);
        return new PricingResult(currentPrice, List.copyOf(appliedRules));
    }
}