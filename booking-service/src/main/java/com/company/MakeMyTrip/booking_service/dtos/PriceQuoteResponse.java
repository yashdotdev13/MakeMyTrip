package com.company.MakeMyTrip.booking_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceQuoteResponse {

    private Long referenceId;
    private String bookingType;
    private Double basePrice;
    private Double adjustedPrice;
    private String currency;
    private List<String> appliedRules;
    private boolean locked;
    private Long expiryTimeSeconds;
}
