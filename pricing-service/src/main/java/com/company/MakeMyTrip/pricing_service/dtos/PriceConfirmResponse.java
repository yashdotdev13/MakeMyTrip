package com.company.MakeMyTrip.pricing_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceConfirmResponse {

    private Long referenceId;
    private String bookingType;
    private Double finalPrice;
    private Long userId;
    private Instant confirmedAt;
}
