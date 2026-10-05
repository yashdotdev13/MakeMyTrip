package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.pricing_service.auth.UserContextHolder;
import com.company.MakeMyTrip.pricing_service.domain.PricingContext;
import com.company.MakeMyTrip.pricing_service.domain.PricingResult;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.pricing_service.engine.PricingEngine;
import com.company.MakeMyTrip.pricing_service.entity.BasePrice;
import com.company.MakeMyTrip.pricing_service.entity.DemandProjection;
import com.company.MakeMyTrip.pricing_service.entity.PriceLock;
import com.company.MakeMyTrip.pricing_service.enums.PriceLockStatus;
import com.company.MakeMyTrip.pricing_service.exceptions.InvalidTravelDateException;
import com.company.MakeMyTrip.pricing_service.repository.DemandProjectionRepository;
import com.company.MakeMyTrip.pricing_service.repository.PriceLockRepository;
import com.company.MakeMyTrip.pricing_service.service.BasePriceService;
import com.company.MakeMyTrip.pricing_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingServiceImpl implements PricingService {

    private static final long QUOTE_EXPIRY_SECONDS = 300L;

    private static final int PRICE_LOCK_DURATION_MINUTES = 5;

    private final PriceLockRepository priceLockRepository;

    private final PricingEngine pricingEngine;

    private final BasePriceService basePriceService;

    private final DemandProjectionRepository demandProjectionRepository;

    @Override
    public PriceQuoteResponse getPriceQuote(Long referenceId, String bookingType,
                                            int quantity, String travelDateStr) {
        Long userId = UserContextHolder.getCurrentUserId();

        log.info("Generating price quote: referenceId={}, bookingType={}, quantity={}, userId={}",
                referenceId, bookingType, quantity, userId);

        LocalDate travelDate = parseTravelDate(travelDateStr);
        BasePrice basePriceEntity = basePriceService.getBasePrice(referenceId, bookingType);
        BigDecimal unitBasePrice = basePriceEntity.getPrice();

        BigDecimal basePrice = unitBasePrice.multiply(BigDecimal.valueOf(quantity));
        String currency = basePriceEntity.getCurrency();

        int currentBookings = demandProjectionRepository
                .findByReferenceIdAndBookingTypeAndTravelDate(referenceId,
                        bookingType, travelDate).map(DemandProjection::getCurrentBookings).orElse(0);

        log.debug("Demand projection loaded: referenceId={}, bookingType={}, travelDate={}, currentBookings={}",
                referenceId, bookingType, travelDate, currentBookings);

        PricingContext context = new PricingContext(referenceId, bookingType, basePrice, quantity,
                travelDate, currentBookings);

        PricingResult pricingResult = pricingEngine.calculate(context);
        Instant expiresAt = Instant.now().plusSeconds(QUOTE_EXPIRY_SECONDS);
        log.info("Price calculation completed: referenceId={}, unitBasePrice={}, quantity={}, " +
                "basePrice={}, finalPrice={}, currency={}, appliedRules={}, expiresAt={}",
                referenceId, unitBasePrice, quantity, basePrice, pricingResult.finalPrice(), currency,
                pricingResult.appliedRules(), expiresAt);
        return PriceQuoteResponse.builder().referenceId(referenceId).bookingType(bookingType)
                .basePrice(basePrice).adjustedPrice(pricingResult.finalPrice())
                .currency(currency).appliedRules(pricingResult.appliedRules()).locked(false)
                .expiryTimeSeconds(QUOTE_EXPIRY_SECONDS).expiresAt(expiresAt).build();
    }

    @Override
    public PriceLockResponse lockPrice(PriceLockRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();

        log.info("Locking price: referenceId={}, bookingType={}, quantity={}, travelDate={}, userId={}",
                request.getReferenceId(), request.getBookingType(),
                request.getQuantity(), request.getTravelDate(), userId);

        LocalDate travelDate = parseTravelDate(request.getTravelDate());

        /*
         * Calculate the exact price using the same
         * pricing flow used by the quote endpoint.
         */
        PriceQuoteResponse quote = getPriceQuote(request.getReferenceId(),
                request.getBookingType(), request.getQuantity(), request.getTravelDate());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validTill = now.plusMinutes(PRICE_LOCK_DURATION_MINUTES);

        PriceLock priceLock = PriceLock.builder().referenceId(request.getReferenceId()).
                bookingType(request.getBookingType()).quantity(request.getQuantity()).
                travelDate(travelDate).userId(userId).basePrice(quote.getBasePrice()).
                adjustedPrice(quote.getAdjustedPrice()).status(PriceLockStatus.ACTIVE).
                validTill(validTill).createdAt(now).currency(quote.getCurrency()).build();

        PriceLock savedLock = priceLockRepository.save(priceLock);

        log.info("Price locked successfully: lockId={}, referenceId={}, bookingType={}, quantity={}," +
                " travelDate={}, finalPrice={}, validTill={}", savedLock.getId(), savedLock.getReferenceId(),
                savedLock.getBookingType(), savedLock.getQuantity(), savedLock.getTravelDate(),
                savedLock.getAdjustedPrice(), savedLock.getValidTill());

        return PriceLockResponse.builder().lockId(savedLock.getId())
                .referenceId(savedLock.getReferenceId()).bookingType(savedLock
                        .getBookingType()).lockedPrice(savedLock.getAdjustedPrice())
                .lockExpiryTime(savedLock.getValidTill().atZone(ZoneId.systemDefault())
                        .toInstant()).build();
    }

    @Override
    public boolean releasePriceLock(Long lockId) {
        log.info("Releasing price lock: lockId={}", lockId);

        return priceLockRepository.findById(lockId).map(lock -> {

            if (lock.getStatus() != PriceLockStatus.ACTIVE) {
                log.warn("Price lock is not active: lockId={}, status={}", lockId, lock.getStatus());

                return false;
            }
            lock.setStatus(PriceLockStatus.RELEASED);
            priceLockRepository.save(lock);
            log.info("Price lock released successfully: lockId={}", lockId);

            return true;
        }).orElseGet(() -> {
            log.warn("Price lock not found: lockId={}", lockId);
            return false;
        });
    }

    @Override
    public List<String> getAllPriceRules() {
        return List.of();
    }

    private LocalDate parseTravelDate(String travelDateStr) {
        if (travelDateStr == null || travelDateStr.isBlank()) {

            throw new InvalidTravelDateException("Travel date is required");
        }

        try {

            return LocalDate.parse(travelDateStr);

        } catch (DateTimeParseException exception) {

            throw new InvalidTravelDateException("Travel date must be in yyyy-MM-dd format");
        }
    }
}