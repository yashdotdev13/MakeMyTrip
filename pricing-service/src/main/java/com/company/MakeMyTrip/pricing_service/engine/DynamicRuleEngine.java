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


    public double applyRules(double basePrice, LocalDate travelDate, int quantity){
        double adjustedPrice = basePrice;

        // Fetch all active rules
        List<PriceRule> allRules = priceRuleRepository.findAll();
        if (allRules == null || allRules.isEmpty()) {
            return adjustedPrice;
        }

        for (PriceRule rule : allRules) {
            if (rule.getActive() == null || !rule.getActive()) {
                continue; // skip inactive rules
            }

            switch (rule.getRuleType()) {
                case SEASONAL:
                    if (isWithinSeason(rule, travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Applied SEASONAL rule (factor={}): new price={}", rule.getFactor(), adjustedPrice);
                    }
                    break;

                case WEEKEND:
                    if (isWeekend(travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Applied WEEKEND rule (factor={}): new price={}", rule.getFactor(), adjustedPrice);
                    }
                    break;

                case DEMAND:
                    if (rule.getMinQuantityThreshold() != null && quantity >= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Applied DEMAND rule (factor={}): new price={}", rule.getFactor(), adjustedPrice);
                    }
                    break;

                case SHORTAGE:
                    if (rule.getInventoryType() != null && !rule.getInventoryType().isEmpty()) {
                        // TODO: Fetch actual inventory based on inventoryType and apply factor
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Applied SHORTAGE rule (factor={}): new price={}", rule.getFactor(), adjustedPrice);
                    }
                    break;

                case FESTIVAL:
                    if (isWithinSeason(rule, travelDate)) { // assuming festival dates stored in start/end
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("Applied FESTIVAL rule (factor={}): new price={}", rule.getFactor(), adjustedPrice);
                    }
                    break;

                default:
                    log.warn("Unknown rule type {} skipped", rule.getRuleType());
            }
        }

        return adjustedPrice;
    }

    private boolean isWeekend(LocalDate date) {
        if (date == null) return false;
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isWithinSeason(PriceRule rule, LocalDate travelDate) {
        if (rule.getStartDate() == null || rule.getEndDate() == null || travelDate == null) {
            return false;
        }
        return ( !travelDate.isBefore(rule.getStartDate()) ) &&
                ( !travelDate.isAfter(rule.getEndDate()) );
    }
}
