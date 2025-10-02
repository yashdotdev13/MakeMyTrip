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
public class PriceLockResponse {

    private Long lockId;
    private Long referenceId;
    private String bookingType;
    private Double lockedPrice;
    private Long userId;
    private Instant lockExpiryTime;
}
