package com.company.MakeMyTrip.pricing_service.engine;

import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRuleEngine {

    private final PriceRuleRepository priceRuleRepository;

    public static class RuleApplicationResult {
        private double adjustedPrice;
        private List<String> appliedRules;

        public RuleApplicationResult(double adjustedPrice, List<String> appliedRules) {
            this.adjustedPrice = adjustedPrice;
            this.appliedRules = appliedRules;
        }

        public double getAdjustedPrice() {
            return adjustedPrice;
        }

        public List<String> getAppliedRules() {
            return appliedRules;
        }
    }

    public RuleApplicationResult applyRules(double basePrice, LocalDate travelDate, int quantity, String inventoryType, int availableInventory) {
        double adjustedPrice = basePrice;
        List<String> appliedRules = new ArrayList<>();

        List<PriceRule> allRules = priceRuleRepository.findAll();

        for (PriceRule rule : allRules) {
            if (!rule.getActive()) continue; // skip inactive rules

            switch (rule.getRuleType()) {
                case SEASONAL:
                    if (isWithinSeason(rule, travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        appliedRules.add("Seasonal Rule Applied");
                    }
                    break;

                case FESTIVAL:
                    if (isWithinSeason(rule, travelDate)) { // use startDate/endDate for festival
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        appliedRules.add("Festival Rule Applied");
                    }
                    break;

                case CROWD_DEMAND:
                    adjustedPrice += adjustedPrice * rule.getFactor();
                    appliedRules.add("Crowd Demand Rule Applied");
                    break;

                case SHORTAGE:
                    if (rule.getInventoryType() != null && rule.getInventoryType().equalsIgnoreCase(inventoryType)) {
                        if (availableInventory <= rule.getMinQuantityThreshold()) {
                            adjustedPrice += adjustedPrice * rule.getFactor();
                            appliedRules.add("Shortage Rule Applied");
                        }
                    }
                    break;

                case WEEKEND:
                    if (isWeekend(travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        appliedRules.add("Weekend Rule Applied");
                    }
                    break;

                default:
                    log.warn("Unknown RuleType: {}", rule.getRuleType());
            }
        }

        return new RuleApplicationResult(adjustedPrice, appliedRules);
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
