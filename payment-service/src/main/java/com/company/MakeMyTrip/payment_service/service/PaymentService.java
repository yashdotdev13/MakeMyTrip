package com.company.MakeMyTrip.payment_service.service;

import com.company.MakeMyTrip.events.PaymentRequestedEvent;
import com.company.MakeMyTrip.payment_service.dtos.*;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentRequest request);

    PaymentResponse confirmPayment(PaymentConfirmationRequest request);

    PaymentResponse getPaymentByBookingId(Long bookingId);

    void initiatePayment(PaymentRequestedEvent event);
}