package com.company.MakeMyTrip.pricing_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceLockRequest {

    private Long referenceId;
    private String bookingType;
    private Double adjustedPrice;
    private Long userId;
    private int lockDurationMinutes;
}
