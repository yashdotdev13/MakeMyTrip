package com.company.MakeMyTrip.payment_service.service.Impl;


import com.company.MakeMyTrip.payment_service.dtos.PaymentConfirmationRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentResponse;
import com.company.MakeMyTrip.payment_service.entity.Payment;
import com.company.MakeMyTrip.payment_service.enums.PaymentStatus;
import com.company.MakeMyTrip.payment_service.repository.PaymentRepository;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
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
        try{
            log.info("Initiating payment for bookingId={} by userId={}",request.getBookingId(), request.getUserId());

            // fetch the Stripe PaymentInit
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (request.getAmount() * 100)) // Stripe expects amount in cents/paise
                    .setCurrency("inr")
                    .putMetadata("bookingId", String.valueOf(request.getBookingId()))
                    .putMetadata("userId", String.valueOf(request.getUserId()))
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            // 2️⃣ Save payment in DB as PENDING
            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .transactionId(paymentIntent.getId())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .bookingId(String.valueOf(savedPayment.getBookingId()))
                    .userId(savedPayment.getUserId())
                    .amount(savedPayment.getAmount())
                    .status(savedPayment.getStatus())
                    .transactionId(savedPayment.getTransactionId())
                    .message("Payment initiated successfully. Confirm the payment to complete.")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage(), e);
            return PaymentResponse.builder()
                    .bookingId(String.valueOf(request.getBookingId()))
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.FAILED)
                    .message("Failed to initiate payment: " + e.getMessage())
                    .build();
        }
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
