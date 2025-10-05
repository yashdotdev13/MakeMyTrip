package com.company.MakeMyTrip.pricing_service.engine;

import com.company.MakeMyTrip.pricing_service.client.BookingServiceClient;
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
    private final BookingServiceClient bookingServiceClient; // Feign client

    public double applyRules(double basePrice, LocalDate travelDate, int quantity, int currentBookings) {
        double adjustedPrice = basePrice;

        List<PriceRule> allRules = priceRuleRepository.findAll();

        for (PriceRule rule : allRules) {
            if (!rule.getActive()) continue;

            switch (rule.getRuleType()) {
                case SEASONAL:
                    if (isWithinSeason(rule, travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("SEASONAL rule applied: factor={}, adjustedPrice={}", rule.getFactor(), adjustedPrice);
                    }
                    break;

                case WEEKEND:
                    if (isWeekend(travelDate)) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("WEEKEND rule applied: factor={}, adjustedPrice={}", rule.getFactor(), adjustedPrice);
                    }
                    break;

                case DEMAND:
                    if (rule.getMinQuantityThreshold() != null && quantity >= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("DEMAND rule applied: minQty={}, factor={}, adjustedPrice={}",
                                rule.getMinQuantityThreshold(), rule.getFactor(), adjustedPrice);
                    }
                    break;

                case CROWD_DEMAND:
                    if (rule.getMinQuantityThreshold() != null && currentBookings >= rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("CROWD_DEMAND rule applied: minBookings={}, factor={}, adjustedPrice={}",
                                rule.getMinQuantityThreshold(), rule.getFactor(), adjustedPrice);
                    }
                    break;

                case SHORTAGE:
                    if (rule.getMinQuantityThreshold() != null && currentBookings < rule.getMinQuantityThreshold()) {
                        adjustedPrice += adjustedPrice * rule.getFactor();
                        log.info("SHORTAGE rule applied: minBookings={}, factor={}, adjustedPrice={}",
                                rule.getMinQuantityThreshold(), rule.getFactor(), adjustedPrice);
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
        return rule.getStartDate() != null && rule.getEndDate() != null
                && !travelDate.isBefore(rule.getStartDate())
                && !travelDate.isAfter(rule.getEndDate());
    }
}
