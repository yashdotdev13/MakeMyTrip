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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(30)
public class WeekendPricingStrategy implements PricingStrategy {

    private final PriceRuleRepository priceRuleRepository;

    @Override
    public StrategyResult apply(PricingContext context) {

        LocalDate travelDate = context.travelDate();

        /*
         * Weekend pricing only applies when the travel date
         * falls on Saturday or Sunday.
         */
        if (travelDate == null || !isWeekend(travelDate)) {
            return StrategyResult.unchanged(
                    context.basePrice()
            );
        }

        List<PriceRule> applicableRules =
                priceRuleRepository.findByRuleType(
                                RuleType.WEEKEND
                        )
                        .stream()
                        .filter(rule -> isApplicable(
                                rule,
                                travelDate
                        ))
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
                new ArrayList<>();

        for (PriceRule rule : applicableRules) {

            BigDecimal factor =
                    rule.getFactor();

            if (factor == null) {

                log.warn(
                        "Skipping weekend pricing rule because factor is null: ruleId={}",
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
                    "Weekend pricing rule applied: ruleId={}, factor={}%, priceBefore={}, adjustment={}, priceAfter={}",
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
        return RuleType.WEEKEND.name();
    }

    private boolean isWeekend(LocalDate date) {

        DayOfWeek dayOfWeek =
                date.getDayOfWeek();

        return dayOfWeek == DayOfWeek.SATURDAY
                || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private boolean isApplicable(
            PriceRule rule,
            LocalDate travelDate
    ) {

        if (!Boolean.TRUE.equals(
                rule.getActive()
        )) {
            return false;
        }

        LocalDate startDate =
                rule.getStartDate();

        LocalDate endDate =
                rule.getEndDate();

        /*
         * If a weekend rule has no date boundaries,
         * it applies to every weekend.
         */
        if (startDate == null && endDate == null) {
            return true;
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