package com.company.MakeMyTrip.booking_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingConfirmationResponse {

    private Long bookingId;
    private String status;
    private BigDecimal finalPrice;
    private String message;
}