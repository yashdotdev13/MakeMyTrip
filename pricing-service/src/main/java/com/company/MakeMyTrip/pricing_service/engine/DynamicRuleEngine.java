package com.company.MakeMyTrip.pricing_service.engine;

import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRuleEngine {

    private final PriceRuleRepository priceRuleRepository;

    public double applyRules(double basePrice, LocalDate travelDate, int quantity, int currentBookings) {
        double adjustedPrice = basePrice;

        List<PriceRule> allRules = priceRuleRepository.findAll();

        for (PriceRule rule : allRules) {
            if (!rule.getActive()) continue;

            switch (rule.getRuleType()) {
                case SEASONAL:
                    if (isWithinSeason(rule, travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                    }
                    break;

                case WEEKEND:
                    if (isWeekend(travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                    }
                    break;

                case DEMAND:
                    if (quantity >= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                    }
                    break;

                case CROWD_DEMAND:
                    if (currentBookings >= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                    }
                    break;

                case SHORTAGE:
                    // For shortage, lower availability triggers higher price
                    if (currentBookings < rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                    }
                    break;

                default:
                    log.warn("Unknown rule type: {}", rule.getRuleType());
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
