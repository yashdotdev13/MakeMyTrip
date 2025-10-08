package com.company.MakeMyTrip.review_service.dtos;



import com.company.MakeMyTrip.review_service.enums.BookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequest {

    private BookingType bookingType;  // FLIGHT, HOTEL, BUS, TRAIN
    private Long referenceId;         // id for the booked entity (flightId, hotelId, etc)
    private Double amount;            // amount for this booking
    private LocalDate travelDate;     // <-- NEW FIELD
}
