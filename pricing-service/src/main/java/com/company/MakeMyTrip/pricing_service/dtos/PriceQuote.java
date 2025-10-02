package com.company.MakeMyTrip.pricing_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceQuote {

    private Long referenceId;
    private String bookingType;
    private Double basePrice;
    private Double adjustedPrice;
    private List<String> appliedRules;
    private boolean locked;
}
