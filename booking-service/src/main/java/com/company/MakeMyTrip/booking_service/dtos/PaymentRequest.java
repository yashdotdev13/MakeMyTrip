package com.company.MakeMyTrip.booking_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private Long bookingId;
    private Long userId;
    private Double amount;
    private String paymentMethod;
}
