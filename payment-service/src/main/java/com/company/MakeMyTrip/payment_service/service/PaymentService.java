package com.company.MakeMyTrip.payment_service.service;

import com.company.MakeMyTrip.payment_service.dtos.*;

public interface PaymentService {


    // initiate payment
    PaymentResponse initiatePayment(PaymentRequest request);


    // confirm a payment
    PaymentResponse confirmPayment(PaymentConfirmationRequest request);

     // fetch payment details by bookingId
    PaymentResponse getPaymentByBookingId(Long bookingId);

    RefundResponse refundPayment(RefundRequest request);
}
