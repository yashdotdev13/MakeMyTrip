package com.company.MakeMyTrip.booking_service.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingConfirmationResponse {

    private Long bookingId;

    private String status;

    private Double finalPrice;
    private String message;
}
