package com.company.MakeMyTrip.payment_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequest {

    private String transactionId;
    private Double amount;
    private String message;
}
