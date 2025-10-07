package com.company.MakeMyTrip.booking_service.client;


import com.company.MakeMyTrip.booking_service.dtos.PaymentRequest;
import com.company.MakeMyTrip.booking_service.dtos.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", path = "/payment")
public interface PaymentClient {

    @PostMapping("/initiate")
    PaymentResponse initiatePayment(@RequestBody PaymentRequest paymentRequest);
}
