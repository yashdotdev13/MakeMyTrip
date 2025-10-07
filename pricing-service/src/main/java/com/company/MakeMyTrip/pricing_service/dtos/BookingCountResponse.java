package com.company.MakeMyTrip.pricing_service.dtos;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCountResponse {
    private Long referenceId;
    private String travelDate;
    private int currentBookings;
}
