package com.company.MakeMyTrip.pricing_service.service;

import com.company.MakeMyTrip.pricing_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.pricing_service.dtos.PriceQuoteResponse;

import java.util.List;

public interface PricingService {


    PriceQuoteResponse getPriceQuote(Long referenceId, String bookingType, int quantity, String travelDate);

    PriceLockResponse lockPrice(Long referenceId, String bookingType);

    boolean releasePriceLock(Long lockId);

    List<String> getAllPriceRules();
}
