package com.company.MakeMyTrip.pricing_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceConfirmRequest {

    private Long lockId;   // lock id to confirm
    private Long userId;  // User confirming the booking
}
