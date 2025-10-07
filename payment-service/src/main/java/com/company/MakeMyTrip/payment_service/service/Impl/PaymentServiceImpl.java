package com.company.MakeMyTrip.payment_service.service.Impl;

import com.company.MakeMyTrip.payment_service.dtos.PaymentConfirmationRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentResponse;
import com.company.MakeMyTrip.payment_service.entity.Payment;
import com.company.MakeMyTrip.payment_service.enums.PaymentStatus;
import com.company.MakeMyTrip.payment_service.repository.PaymentRepository;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        try {
            log.info("Initiating payment for bookingId={} by userId={}", request.getBookingId(), request.getUserId());

            // RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", 1000); // in paise
            options.put("currency", "INR");
            options.put("receipt", "receipt_123");
            options.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(options); // note lowercase 'orders'


            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .transactionId(order.get("id"))
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .bookingId(String.valueOf(savedPayment.getBookingId()))
                    .userId(savedPayment.getUserId())
                    .amount(savedPayment.getAmount())
                    .status(savedPayment.getStatus())
                    .transactionId(savedPayment.getTransactionId())
                    .message("Payment order created successfully. Complete payment on Razorpay.")
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay error: {}", e.getMessage(), e);
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
        Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Payment not found with transactionId " + request.getTransactionId()));

        // For test environment, we can mark payment as SUCCESS manually
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(String.valueOf(payment.getBookingId()))
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .message("Payment confirmed successfully.")
                .build();
    }

    @Override
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found for bookingId " + bookingId));

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(String.valueOf(payment.getBookingId()))
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .message("Payment details fetched successfully.")
                .build();
    }
}
