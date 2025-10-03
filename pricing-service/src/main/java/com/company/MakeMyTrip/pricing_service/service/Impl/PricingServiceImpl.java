package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.pricing_service.auth.UserContextHolder;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.pricing_service.entity.PriceLock;
import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.repository.PriceLockRepository;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import com.company.MakeMyTrip.pricing_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PriceRuleRepository priceRuleRepository;
    private final PriceLockRepository priceLockRepository;
    private final ModelMapper modelMapper;

    private static final double DEFAULT_BASE_PRICE = 1000.0;

    @Override
    public PriceQuoteResponse getPriceQuote(Long referenceId, String bookingType, int quantity, String travelDate) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Generating price quote for refId: {}, bookingType: {}, userId: {}", referenceId, bookingType, userId);

        double basePrice = DEFAULT_BASE_PRICE * quantity;
        double adjustedPrice = basePrice;
        List<String> appliedRules = new ArrayList<>();

        // Fetch all active rules
        List<PriceRule> rules = priceRuleRepository.findAll();

        // Apply rules dynamically
        for (PriceRule rule : rules) {
            switch (rule.getRuleType()) {
                case CROWD_DEMAND:
                    adjustedPrice += (basePrice * rule.getFactor());
                    appliedRules.add("Crowd Demand Rule Applied");
                    break;
                case SEASONAL:
                    // TODO: Apply seasonal pricing based on travelDate
                    adjustedPrice += (basePrice * rule.getFactor());
                    appliedRules.add("Seasonal Pricing Rule Applied");
                    break;
                case FESTIVAL:
                    // TODO: Apply festival pricing based on travelDate
                    adjustedPrice += (basePrice * rule.getFactor());
                    appliedRules.add("Festival Pricing Rule Applied");
                    break;
                case SHORTAGE:
                    // TODO: Fetch inventory and apply shortage factor
                    adjustedPrice += (basePrice * rule.getFactor());
                    appliedRules.add("Shortage Rule Applied");
                    break;
                default:
                    appliedRules.add("Unknown Rule Skipped");
            }
        }

        log.info("Price calculated: base={}, adjusted={}, appliedRules={}", basePrice, adjustedPrice, appliedRules);

        return PriceQuoteResponse.builder()
                .referenceId(referenceId)
                .bookingType(bookingType)
                .basePrice(basePrice)
                .adjustedPrice(adjustedPrice)
                .currency("INR")
                .appliedRules(appliedRules)
                .locked(false)
                .expiryTimeSeconds(300L)
                .build();
    }

    @Override
    public PriceLockResponse lockPrice(Long referenceId, String bookingType) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Locking price for refId={}, bookingType={}, userId={}", referenceId, bookingType, userId);

        // Use quantity = 1 or fetch intended quantity from request/context
        PriceQuoteResponse quote = getPriceQuote(referenceId, bookingType, 1, null);

        PriceLock lock = PriceLock.builder()
                .referenceId(referenceId)
                .bookingType(bookingType)
                .userId(userId)
                .basePrice(quote.getBasePrice())
                .adjustedPrice(quote.getAdjustedPrice())  // <--- use quote's adjusted price
                .locked(true)
                .validTill(LocalDateTime.now().plusMinutes(5)) // 5 mins lock
                .currency(quote.getCurrency())
                .build();

        PriceLock savedLock = priceLockRepository.save(lock);

        return PriceLockResponse.builder()
                .lockId(savedLock.getId())
                .referenceId(referenceId)
                .bookingType(bookingType)
                .lockedPrice(savedLock.getAdjustedPrice())
                .userId(userId)
                .lockExpiryTime(savedLock.getValidTill().atZone(java.time.ZoneId.systemDefault()).toInstant())
                .build();
    }


    @Override
    public boolean releasePriceLock(Long lockId) {
        log.info("Releasing price lock with lockId={}", lockId);

        return priceLockRepository.findById(lockId).map(lock -> {
            priceLockRepository.delete(lock);
            log.info("Price lock {} released successfully", lockId);
            return true;
        }).orElseGet(() -> {
            log.warn("Price lock {} not found for release", lockId);
            return false;
        });
    }

    @Override
    public List<String> getAllPriceRules() {
        log.info("Fetching all pricing rules");
        return priceRuleRepository.findAll().stream()
                .map(rule -> rule.getRuleType().name() + " (" + rule.getFactor() + ")")
                .collect(Collectors.toList());
    }
}
