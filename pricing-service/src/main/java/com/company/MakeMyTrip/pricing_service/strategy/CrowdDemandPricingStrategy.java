package com.company.MakeMyTrip.pricing_service.strategy;

import com.company.MakeMyTrip.pricing_service.domain.PricingContext;
import com.company.MakeMyTrip.pricing_service.domain.PricingStrategy;
import com.company.MakeMyTrip.pricing_service.domain.StrategyResult;
import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(50)
public class CrowdDemandPricingStrategy implements PricingStrategy {

    private final PriceRuleRepository priceRuleRepository;

    @Override
    public StrategyResult apply(PricingContext context) {

        int currentBookings = context.currentBookings();

        if (currentBookings <= 0) {
            return StrategyResult.unchanged(context.basePrice());
        }

        List<PriceRule> applicableRules = priceRuleRepository.findByRuleType(RuleType.CROWD_DEMAND)
                .stream()
                .filter(this::isApplicable)
                .filter(rule -> matchesCrowdThreshold(rule, currentBookings))
                .sorted(Comparator.comparing(PriceRule::getMinQuantityThreshold,
                        Comparator.nullsLast(Comparator.naturalOrder()))).toList();

        if (applicableRules.isEmpty()) {
            return StrategyResult.unchanged(context.basePrice());
        }

        BigDecimal adjustedPrice = context.basePrice();

        List<String> appliedRules = new ArrayList<>();

        for (PriceRule rule : applicableRules) {

            BigDecimal factor = rule.getFactor();

            if (factor == null) {

                log.warn("Skipping crowd demand pricing rule because factor is null: ruleId={}", rule.getId());

                continue;
            }

            BigDecimal priceBefore = adjustedPrice;

            BigDecimal adjustment = calculateAdjustment(adjustedPrice, factor);

            adjustedPrice = adjustedPrice.add(adjustment);

            appliedRules.add(buildRuleDescription(rule));

            log.debug("Crowd demand pricing rule applied: ruleId={}, currentBookings={}," +
                    " threshold={}, factor={}%, priceBefore={}, adjustment={}, priceAfter={}",
                    rule.getId(), currentBookings, rule.getMinQuantityThreshold(),
                    factor, priceBefore, adjustment, adjustedPrice);
        }

        if (appliedRules.isEmpty()) {
            return StrategyResult.unchanged(context.basePrice());
        }

        return new StrategyResult(adjustedPrice, List.copyOf(appliedRules));
    }

    @Override
    public String getRuleName() {
        return RuleType.CROWD_DEMAND.name();
    }

    private boolean isApplicable(PriceRule rule) {

        return Boolean.TRUE.equals(rule.getActive());
    }

    private boolean matchesCrowdThreshold(PriceRule rule, int currentBookings) {

        Integer threshold = rule.getMinQuantityThreshold();

        if (threshold == null) {
            return false;
        }

        return currentBookings >= threshold;
    }

    private BigDecimal calculateAdjustment(BigDecimal currentPrice, BigDecimal factor) {
        return currentPrice.multiply(factor).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    private String buildRuleDescription(PriceRule rule) {
        return rule.getRuleType().name() + " (" + rule.getFactor()
                + "%, threshold=" + rule.getMinQuantityThreshold() + ")";
    }
}