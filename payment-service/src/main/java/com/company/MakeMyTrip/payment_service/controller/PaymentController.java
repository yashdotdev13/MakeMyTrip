package com.company.MakeMyTrip.payment_service.controller;

import com.company.MakeMyTrip.payment_service.advices.ApiResponse;
import com.company.MakeMyTrip.payment_service.dtos.*;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // confirm the payment after client completes stripe payment
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmationRequest request){
        log.info("Confirming payment with transactionid={}",request .getTransactionId());
        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(@PathVariable Long bookingId){
        log.info("Fetching payment details for bookingId={}",bookingId);
        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> refundPayment(@RequestBody RefundRequest request) {
        RefundResponse response = paymentService.refundPayment(request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

}
