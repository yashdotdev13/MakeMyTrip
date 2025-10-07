package com.company.MakeMyTrip.payment_service.dtos;


import com.company.MakeMyTrip.payment_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundResponse {


    private Long paymentId;
    private Long transactionId;
    private Double amount;
    private PaymentStatus status;
    private String refundId;
    private String message;
}
