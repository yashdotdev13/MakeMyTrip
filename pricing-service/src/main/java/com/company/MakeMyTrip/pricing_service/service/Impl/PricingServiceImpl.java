package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.pricing_service.auth.UserContextHolder;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.pricing_service.engine.DynamicRuleEngine;
import com.company.MakeMyTrip.pricing_service.entity.PriceLock;
import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.repository.PriceLockRepository;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import com.company.MakeMyTrip.pricing_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PriceRuleRepository priceRuleRepository;
    private final PriceLockRepository priceLockRepository;
    private final ModelMapper modelMapper;
    private final DynamicRuleEngine dynamicRuleEngine;

    private static final double DEFAULT_BASE_PRICE = 1000.0;

    @Override
    public PriceQuoteResponse getPriceQuote(Long referenceId, String bookingType, int quantity, String travelDateStr) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Generating price quote for refId: {}, bookingType: {}, userId: {}", referenceId, bookingType, userId);

        double basePrice = DEFAULT_BASE_PRICE * quantity;
        LocalDate travelDate = travelDateStr != null ? LocalDate.parse(travelDateStr) : LocalDate.now();

        // Use DynamicRuleEngine to calculate adjusted price
        double adjustedPrice = dynamicRuleEngine.applyRules(basePrice, travelDate, quantity);

        // Optional: Collect applied rules for logging/display
        List<String> appliedRules = priceRuleRepository.findAll().stream()
                .filter(PriceRule::getActive)
                .map(rule -> rule.getRuleType().name() + " (" + rule.getFactor() + ")")
                .toList();

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

        // Fetch quote for locking (quantity = 1 by default)
        PriceQuoteResponse quote = getPriceQuote(referenceId, bookingType, 1, null);

        PriceLock lock = PriceLock.builder()
                .referenceId(referenceId)
                .bookingType(bookingType)
                .userId(userId)
                .basePrice(quote.getBasePrice())
                .adjustedPrice(quote.getAdjustedPrice())  // use the dynamically calculated price
                .locked(true)
                .validTill(LocalDateTime.now().plusMinutes(5)) // 5 min lock
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
                .filter(PriceRule::getActive)
                .map(rule -> rule.getRuleType().name() + " (" + rule.getFactor() + ")")
                .collect(Collectors.toList());
    }
}
