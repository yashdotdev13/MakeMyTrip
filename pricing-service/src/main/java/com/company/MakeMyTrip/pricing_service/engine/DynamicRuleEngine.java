package com.company.MakeMyTrip.pricing_service.engine;

import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
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

    /**
     * Apply all rules dynamically to calculate adjusted price
     *
     * @param basePrice    base price of the booking
     * @param travelDate   date of travel
     * @param quantity     number of tickets/rooms booked
     * @param availableQty available inventory for the booking
     * @return adjusted price after applying all rules
     */
    public double applyRules(double basePrice, LocalDate travelDate, int quantity, int availableQty) {
        double adjustedPrice = basePrice;

        List<PriceRule> allRules = priceRuleRepository.findAll();

        for (PriceRule rule : allRules) {
            switch (rule.getRuleType()) {
                case SEASONAL:
                    if (isWithinSeason(rule, travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Seasonal rule applied: factor={}", rule.getFactor());
                    }
                    break;

                case WEEKEND:
                    if (isWeekend(travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Weekend rule applied: factor={}", rule.getFactor());
                    }
                    break;

                case DEMAND:
                    if (quantity >= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Demand rule applied: factor={}", rule.getFactor());
                    }
                    break;

                case SHORTAGE:
                    if (availableQty <= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Shortage rule applied: factor={}", rule.getFactor());
                    }
                    break;

                case FESTIVAL:
                    if (isWithinSeason(rule, travelDate)) { // Using same start/end date logic
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Festival rule applied: factor={}", rule.getFactor());
                    }
                    break;

                default:
                    log.info("Unknown rule skipped: {}", rule.getRuleType());
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
                && (travelDate.isEqual(rule.getStartDate()) || travelDate.isAfter(rule.getStartDate()))
                && (travelDate.isEqual(rule.getEndDate()) || travelDate.isBefore(rule.getEndDate()));
    }
}
