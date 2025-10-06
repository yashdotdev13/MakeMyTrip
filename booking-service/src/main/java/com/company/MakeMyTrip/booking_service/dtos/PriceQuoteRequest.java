package com.company.MakeMyTrip.booking_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceQuoteRequest {

    private Long referenceId;   // e.g. flightId, hotelId, busId etc.
    private String bookingType; // FLIGHT, HOTEL, BUS, TRAIN
    private Long userId;        // who is asking for the quote
    private int quantity;       // number of tickets/rooms
    private String travelDate;  // yyyy-MM-dd format
}