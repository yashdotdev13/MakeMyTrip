package com.company.MakeMyTrip.pricing_service.service;

import com.company.MakeMyTrip.pricing_service.dtos.PriceLockRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;

import java.util.List;

public interface PricingService {

    PriceQuoteResponse getPriceQuote(
            Long referenceId,
            String bookingType,
            int quantity,
            String travelDate
    );

    PriceLockResponse lockPrice(
            PriceLockRequest request
    );

    boolean releasePriceLock(
            Long lockId
    );

    List<String> getAllPriceRules();
}