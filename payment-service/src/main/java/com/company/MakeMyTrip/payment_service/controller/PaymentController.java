package com.company.MakeMyTrip.payment_service.controller;

import com.company.MakeMyTrip.payment_service.dtos.PaymentRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentResponse;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // initiate payment for a booking
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest request){
        log.info("Initiating payment for bookingId={}, userId={}",request.getBookingId(), request.getUserId());

        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }
}
