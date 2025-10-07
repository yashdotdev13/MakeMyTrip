package com.company.MakeMyTrip.booking_service.dtos;


import com.company.MakeMyTrip.payment_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {

    private Long paymentId;
    private String bookingId;
    private Long userId;
    private Double amount;
    private PaymentStatus status;
    private String transactionId;
    private String message;
}
