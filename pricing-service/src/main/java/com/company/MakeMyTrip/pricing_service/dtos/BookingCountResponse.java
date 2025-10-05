package com.company.MakeMyTrip.pricing_service.dtos;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingCountResponse {
    private Long referenceId;
    private String travelDate;
    private int currentBookings;
}
