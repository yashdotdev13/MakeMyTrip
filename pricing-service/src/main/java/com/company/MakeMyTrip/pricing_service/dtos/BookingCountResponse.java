package com.company.MakeMyTrip.pricing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingCountResponse {

    private Long referenceId;

    private String travelDate;

    private int currentBookings;
}