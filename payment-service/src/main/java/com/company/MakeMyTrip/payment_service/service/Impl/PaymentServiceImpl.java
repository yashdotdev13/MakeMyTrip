package com.company.MakeMyTrip.payment_service.service.Impl;

import com.company.MakeMyTrip.payment_service.auth.UserContextHolder;
import com.company.MakeMyTrip.payment_service.dtos.*;
import com.company.MakeMyTrip.payment_service.entity.Payment;
import com.company.MakeMyTrip.payment_service.enums.PaymentStatus;
import com.company.MakeMyTrip.payment_service.repository.PaymentRepository;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import org.json.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

            Long userId = UserContextHolder.getCurrentUserId();
            log.info("Initiating payment for bookingId={} by userId={}", request.getBookingId(), userId);

            // RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", 1000); // in paise
            options.put("currency", "INR");
            options.put("receipt", "receipt_123");
            options.put("payment_capture", 1);
            Order order = razorpayClient.orders.create(options); // note lowercase 'orders'


            // Save payment in DB as PENDING
            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(userId) // <-- use context userId
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
                    .userId(UserContextHolder.getCurrentUserId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.FAILED)
                    .message("Failed to initiate payment: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentResponse confirmPayment(PaymentConfirmationRequest request) {
        Long userId = UserContextHolder.getCurrentUserId(); // <-- fetch from context
        Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Payment not found with transactionId " + request.getTransactionId()));

        // For testing, mark as SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        log.info("Payment confirmed for transactionId={} by userId={}", request.getTransactionId(), userId);

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
        Long userId = UserContextHolder.getCurrentUserId(); // <-- fetch from context
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

    @Override
    public RefundResponse refundPayment(RefundRequest request) {
        try {

            Long userId = UserContextHolder.getCurrentUserId();
            log.info("Processing refund for transactionId={} amount={}", request.getTransactionId(), request.getAmount());

            // 1️⃣ Fetch payment from DB
            Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                    .orElseThrow(() -> new RuntimeException("Payment not found for transactionId " + request.getTransactionId()));

            // 2️⃣ Create refund on Razorpay
            // Create refund using Razorpay Java SDK
            JSONObject options = new JSONObject();
            options.put("amount", (int)(request.getAmount() * 100)); // amount in paise
            options.put("speed", "optimum"); // or "instant"
            options.put("notes", request.getMessage());

           Refund refund = razorpayClient.payments.refund(payment.getTransactionId(), options);


            // 3️⃣ Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // 4️⃣ Build response
            return RefundResponse.builder()
                    .paymentId(payment.getId())
                    .transactionId(Long.valueOf(payment.getTransactionId()))
                    .amount(request.getAmount())
                    .status(payment.getStatus())
                    .refundId(refund.get("id"))
                    .message("Refund processed successfully")
                    .build();

        } catch (RazorpayException e) {
            log.error("Refund failed for transactionId={} : {}", request.getTransactionId(), e.getMessage(), e);
            return RefundResponse.builder()
                    .transactionId(Long.valueOf(request.getTransactionId()))
                    .amount(request.getAmount())
                    .status(PaymentStatus.FAILED)
                    .message("Refund failed: " + e.getMessage())
                    .build();
        }
    }
}
