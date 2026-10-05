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
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(20)
public class SeasonalPricingStrategy implements PricingStrategy {

    private final PriceRuleRepository priceRuleRepository;

    @Override
    public StrategyResult apply(PricingContext context) {

        LocalDate travelDate = context.travelDate();

        if (travelDate == null) {
            return StrategyResult.unchanged(
                    context.basePrice()
            );
        }

        List<PriceRule> applicableRules =
                priceRuleRepository.findByRuleType(
                                RuleType.SEASONAL
                        )
                        .stream()
                        .filter(rule -> isApplicable(rule, travelDate))
                        .sorted(
                                Comparator.comparing(
                                        PriceRule::getId
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
                new java.util.ArrayList<>();

        for (PriceRule rule : applicableRules) {

            BigDecimal factor = rule.getFactor();

            if (factor == null) {
                log.warn(
                        "Skipping seasonal pricing rule because factor is null: ruleId={}",
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
                    "Seasonal pricing rule applied: ruleId={}, factor={}%, priceBefore={}, adjustment={}, priceAfter={}",
                    rule.getId(),
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
        return RuleType.SEASONAL.name();
    }

    private boolean isApplicable(
            PriceRule rule,
            LocalDate travelDate
    ) {

        if (!Boolean.TRUE.equals(rule.getActive())) {
            return false;
        }

        LocalDate startDate =
                rule.getStartDate();

        LocalDate endDate =
                rule.getEndDate();

        /*
         * A seasonal rule needs at least one boundary.
         *
         * If neither date exists, we cannot determine the
         * seasonal period.
         */
        if (startDate == null && endDate == null) {
            return false;
        }

        if (startDate != null
                && travelDate.isBefore(startDate)) {

            return false;
        }

        if (endDate != null
                && travelDate.isAfter(endDate)) {

            return false;
        }

        return true;
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
                + "%)";
    }
}