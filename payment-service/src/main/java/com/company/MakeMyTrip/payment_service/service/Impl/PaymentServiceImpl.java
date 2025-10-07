package com.company.MakeMyTrip.payment_service.service.Impl;


import com.company.MakeMyTrip.payment_service.dtos.PaymentConfirmationRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentResponse;
import com.company.MakeMyTrip.payment_service.repository.PaymentRepository;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private static final String STRIPE_API_KEY = "sk_test_XXXXXXXXXXXXXXXXXXXXXXXX"; // Replace with your test secret key


    @PostConstruct
    public void init(){
        Stripe.apiKey = STRIPE_API_KEY;
    }
    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        return null;
    }

    @Override
    public PaymentResponse confirmPayment(PaymentConfirmationRequest request) {
        return null;
    }

    @Override
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        return null;
    }
}
