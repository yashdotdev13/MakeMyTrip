package com.company.MakeMyTrip.pricing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceConfirmResponse {

    private Long referenceId;

    private String bookingType;

    private BigDecimal finalPrice;

    private Long userId;

    private Instant confirmedAt;
}