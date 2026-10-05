package com.company.MakeMyTrip.pricing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceQuoteResponse {

    private Long referenceId;

    private String bookingType;

    private BigDecimal basePrice;

    private BigDecimal adjustedPrice;

    private String currency;

    private List<String> appliedRules;

    private boolean locked;

    private Long expiryTimeSeconds;

    private Instant expiresAt;
}