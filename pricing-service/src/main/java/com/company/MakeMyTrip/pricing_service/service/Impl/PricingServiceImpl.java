package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.pricing_service.entity.PriceLock;
import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.repository.PriceLockRepository;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import com.company.MakeMyTrip.pricing_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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

    private static final double DEFAULT_BASE_PRICE = 1000.0; // TODO: integrate with booking service


    @Override
    public PriceQuoteResponse getPriceQuote(Long referenceId, String bookingType, Long userId, int quantity, String travelDate) {
        log.info("Generating price quote for refId: {}, bookingType: {}, userId: {}", referenceId, bookingType, userId);

        double basePrice = DEFAULT_BASE_PRICE * quantity; // base * quantity
        double adjustedPrice = basePrice;
        List<String> appliedRules = new ArrayList<>();

        // fetch active rules
        List<PriceRule> rules = priceRuleRepository.findAll();

        // apply rules dynamically
        for (PriceRule rule : rules) {
            switch (rule.getRuleType()) {
                case CROWD_DEMAND:
                    adjustedPrice += (basePrice * rule.getFactor());
                    appliedRules.add("Crowd Demand Rule Applied");
                    break;
                case SEASONAL:
                    // TODO: check travelDate and apply seasonal factor
                    adjustedPrice += (basePrice * rule.getFactor());
                    appliedRules.add("Seasonal Pricing Rule Applied");
                    break;
                case FESTIVAL:
                    // TODO: detect if travelDate is in a festival period
                    adjustedPrice += (basePrice * rule.getFactor());
                    appliedRules.add("Festival Pricing Rule Applied");
                    break;
                case SHORTAGE:
                    // TODO: fetch available seats/rooms from inventory
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
                .expiryTimeSeconds(300L) // 5 mins validity for quote
                .build();
    }


    @Override
    public PriceLockResponse lockPrice(Long referenceId, String bookingType, Long userId) {
        log.info("Locking price for refId: {}, bookingType: {}, userId: {}", referenceId, bookingType, userId);

        // TODO: Ideally fetch latest adjusted price first using getPriceQuote()
        double lockedPrice = DEFAULT_BASE_PRICE;

        PriceLock lock = PriceLock.builder()
                .referenceId(referenceId)
                .bookingType(bookingType)
                .userId(userId)
                .basePrice(lockedPrice)
                .adjustedPrice(lockedPrice) // final after adjustments
                .locked(true)
                .validTill(LocalDateTime.now().plusMinutes(5)) // lock valid for 5 mins
                .build();

        PriceLock savedLock = priceLockRepository.save(lock);

        log.info("Price locked successfully for userId={} with lockId={}", userId, savedLock.getId());

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
