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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(70)
public class LastMinutePricingStrategy implements PricingStrategy {

    private final PriceRuleRepository priceRuleRepository;

    @Override
    public StrategyResult apply(PricingContext context) {

        LocalDate travelDate =
                context.travelDate();

        if (travelDate == null) {
            return StrategyResult.unchanged(
                    context.basePrice()
            );
        }

        long daysUntilTravel =
                ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        travelDate
                );

        /*
         * A last-minute rule only makes sense for today
         * or a future travel date.
         */
        if (daysUntilTravel < 0) {
            return StrategyResult.unchanged(
                    context.basePrice()
            );
        }

        List<PriceRule> applicableRules =
                priceRuleRepository.findByRuleType(
                                RuleType.LAST_MINUTE
                        )
                        .stream()
                        .filter(this::isActive)
                        .filter(rule ->
                                matchesLastMinuteThreshold(
                                        rule,
                                        daysUntilTravel
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        PriceRule::getMinQuantityThreshold,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                        )
                        .toList();

        if (applicableRules.isEmpty()) {
            return StrategyResult.unchanged(
                    context.basePrice()
            );
        }

        BigDecimal adjustedPrice =
                context.basePrice();

        List<String> appliedRules =
                new ArrayList<>();

        for (PriceRule rule : applicableRules) {

            BigDecimal factor =
                    rule.getFactor();

            if (factor == null) {

                log.warn(
                        "Skipping last-minute pricing rule because factor is null: ruleId={}",
                        rule.getId()
                );

                continue;
            }

            BigDecimal priceBefore =
                    adjustedPrice;

            BigDecimal adjustment =
                    calculateAdjustment(
                            adjustedPrice,
                            factor
                    );

            adjustedPrice =
                    adjustedPrice.add(adjustment);

            appliedRules.add(
                    buildRuleDescription(rule)
            );

            log.debug(
                    "Last-minute pricing rule applied: ruleId={}, daysUntilTravel={}, threshold={}, factor={}%, priceBefore={}, adjustment={}, priceAfter={}",
                    rule.getId(),
                    daysUntilTravel,
                    rule.getMinQuantityThreshold(),
                    factor,
                    priceBefore,
                    adjustment,
                    adjustedPrice
            );
        }

        if (appliedRules.isEmpty()) {
            return StrategyResult.unchanged(
                    context.basePrice()
            );
        }

        return new StrategyResult(
                adjustedPrice,
                List.copyOf(appliedRules)
        );
    }

    @Override
    public String getRuleName() {
        return RuleType.LAST_MINUTE.name();
    }

    private boolean isActive(
            PriceRule rule
    ) {

        return Boolean.TRUE.equals(
                rule.getActive()
        );
    }

    private boolean matchesLastMinuteThreshold(
            PriceRule rule,
            long daysUntilTravel
    ) {

        Integer threshold =
                rule.getMinQuantityThreshold();

        if (threshold == null) {
            return false;
        }

        /*
         * For LAST_MINUTE rules, the threshold represents
         * the maximum number of days before travel at which
         * the rule becomes applicable.
         *
         * Example:
         *
         * threshold = 3
         *
         * Travel in:
         * 0 days -> applies
         * 1 day  -> applies
         * 2 days -> applies
         * 3 days -> applies
         * 4 days -> does not apply
         */
        return daysUntilTravel <= threshold;
    }

    private BigDecimal calculateAdjustment(
            BigDecimal currentPrice,
            BigDecimal factor
    ) {

        return currentPrice
                .multiply(factor)
                .divide(
                        BigDecimal.valueOf(100),
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private String buildRuleDescription(
            PriceRule rule
    ) {

        return rule.getRuleType().name()
                + " ("
                + rule.getFactor()
                + "%, within "
                + rule.getMinQuantityThreshold()
                + " days)";
    }
}