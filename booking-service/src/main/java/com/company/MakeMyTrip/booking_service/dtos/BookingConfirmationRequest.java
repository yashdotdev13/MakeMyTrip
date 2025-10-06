package com.company.MakeMyTrip.booking_service.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingConfirmationRequest {

    private Long bookingId;
    private Double quotedPrice;
}
