package com.company.MakeMyTrip.pricing_service.engine;


import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRuleEngine {

    private final PriceRuleRepository priceRuleRepository;


    // main method that applies all rules dynamically

    public double applyRules(double basePrice, LocalDate travelDate, int quantity){
        double adjustedPrice = basePrice;

        List<PriceRule> allRules = priceRuleRepository.findAll();

        for (PriceRule rule : allRules) {
            switch (rule.getRuleType()) {
                case SEASONAL:
                    if (isWithinSeason(rule, travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getAdjustmentPercentage() / 100;
                    }
                    break;

                case WEEKEND:
                    if (isWeekend(travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getAdjustmentPercentage() / 100;
                    }
                    break;

                case DEMAND:
                    if (quantity >= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getAdjustmentPercentage() / 100;
                    }
                    break;
            }
        }

        return adjustedPrice;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isWithinSeason(PriceRule rule, LocalDate travelDate) {
        return (rule.getStartDate() != null && rule.getEndDate() != null)
                && (travelDate.isAfter(rule.getStartDate()) || travelDate.isEqual(rule.getStartDate()))
                && (travelDate.isBefore(rule.getEndDate()) || travelDate.isEqual(rule.getEndDate()));
    }
}
