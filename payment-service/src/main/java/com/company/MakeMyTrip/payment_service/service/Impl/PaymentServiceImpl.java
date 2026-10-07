package com.company.MakeMyTrip.payment_service.service.Impl;

import com.company.MakeMyTrip.events.PaymentRequestedEvent;
import com.company.MakeMyTrip.payment_service.dtos.PaymentConfirmationRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentRequest;
import com.company.MakeMyTrip.payment_service.dtos.PaymentResponse;
import com.company.MakeMyTrip.payment_service.entity.Payment;
import com.company.MakeMyTrip.payment_service.enums.PaymentStatus;
import com.company.MakeMyTrip.payment_service.kafka.PaymentEventPublisher;
import com.company.MakeMyTrip.payment_service.repository.PaymentRepository;
import com.company.MakeMyTrip.payment_service.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentEventPublisher paymentEventPublisher;

    /**
     * Legacy REST-based payment initiation.
     *
     * Kept temporarily for backward compatibility.
     * The new flow uses PaymentRequestedEvent through Kafka.
     */
    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {

        try {

            log.info(
                    "Initiating payment for bookingId={} by userId={}",
                    request.getBookingId(),
                    request.getUserId()
            );

            JSONObject options = new JSONObject();

            long amountInPaise = BigDecimal.valueOf(request.getAmount())
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put(
                    "receipt",
                    "booking_" + request.getBookingId()
            );
            options.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(options);

            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(request.getPaymentMethod())
                    .transactionId(order.get("id"))
                    .paymentTime(LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .bookingId(String.valueOf(savedPayment.getBookingId()))
                    .userId(savedPayment.getUserId())
                    .amount(savedPayment.getAmount())
                    .status(savedPayment.getStatus())
                    .transactionId(savedPayment.getTransactionId())
                    .message(
                            "Payment order created successfully. Complete payment on Razorpay."
                    )
                    .build();

        } catch (RazorpayException | ArithmeticException exception) {

            log.error(
                    "Payment initiation failed for bookingId={}",
                    request.getBookingId(),
                    exception
            );

            return PaymentResponse.builder()
                    .bookingId(String.valueOf(request.getBookingId()))
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.FAILED)
                    .message(
                            "Failed to initiate payment: "
                                    + exception.getMessage()
                    )
                    .build();
        }
    }

    /**
     * Confirms an existing payment.
     *
     * After the payment is persisted as SUCCESS,
     * PaymentCompletedEvent is published to Kafka.
     *
     * If confirmation fails, PaymentFailedEvent is published.
     */
    @Override
    public PaymentResponse confirmPayment(
            PaymentConfirmationRequest request
    ) {

        log.info(
                "Confirming payment with transactionId={}",
                request.getTransactionId()
        );

        Payment payment = paymentRepository
                .findByTransactionId(request.getTransactionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found with transactionId "
                                        + request.getTransactionId()
                        )
                );

        try {

            /*
             * Temporary confirmation logic.
             *
             * Later this will be replaced with actual
             * Razorpay payment verification/webhook handling.
             */
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentTime(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            paymentEventPublisher.publishPaymentCompleted(
                    savedPayment.getId(),
                    savedPayment.getBookingId(),
                    savedPayment.getUserId(),
                    BigDecimal.valueOf(savedPayment.getAmount()),
                    savedPayment.getTransactionId()
            );

            log.info(
                    "Payment confirmed successfully: paymentId={}, bookingId={}, transactionId={}",
                    savedPayment.getId(),
                    savedPayment.getBookingId(),
                    savedPayment.getTransactionId()
            );

            return PaymentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .bookingId(
                            String.valueOf(savedPayment.getBookingId())
                    )
                    .userId(savedPayment.getUserId())
                    .amount(savedPayment.getAmount())
                    .status(savedPayment.getStatus())
                    .transactionId(savedPayment.getTransactionId())
                    .message("Payment confirmed successfully.")
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Payment confirmation failed: transactionId={}",
                    request.getTransactionId(),
                    exception
            );

            payment.setStatus(PaymentStatus.FAILED);

            Payment failedPayment = paymentRepository.save(payment);

            paymentEventPublisher.publishPaymentFailed(
                    failedPayment.getBookingId(),
                    failedPayment.getUserId(),
                    BigDecimal.valueOf(failedPayment.getAmount()),
                    exception.getMessage()
            );

            return PaymentResponse.builder()
                    .paymentId(failedPayment.getId())
                    .bookingId(
                            String.valueOf(failedPayment.getBookingId())
                    )
                    .userId(failedPayment.getUserId())
                    .amount(failedPayment.getAmount())
                    .status(failedPayment.getStatus())
                    .transactionId(failedPayment.getTransactionId())
                    .message(
                            "Payment confirmation failed: "
                                    + exception.getMessage()
                    )
                    .build();
        }
    }

    /**
     * Fetch payment details using booking ID.
     */
    @Override
    public PaymentResponse getPaymentByBookingId(Long bookingId) {

        Payment payment = paymentRepository
                .findByBookingId(bookingId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for bookingId "
                                        + bookingId
                        )
                );

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

    /**
     * Kafka-driven payment initiation.
     *
     * Booking Service publishes PaymentRequestedEvent.
     * Payment Service consumes that event and creates
     * the Razorpay order.
     */
    @Override
    public void initiatePayment(PaymentRequestedEvent event) {

        log.info(
                "Processing payment request: bookingId={}, userId={}, amount={}, paymentMethod={}",
                event.bookingId(),
                event.userId(),
                event.amount(),
                event.paymentMethod()
        );

        try {

            JSONObject options = new JSONObject();

            long amountInPaise = event.amount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put(
                    "receipt",
                    "booking_" + event.bookingId()
            );
            options.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(options);

            Payment payment = Payment.builder()
                    .bookingId(event.bookingId())
                    .userId(event.userId())
                    .amount(event.amount().doubleValue())
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(event.paymentMethod())
                    .transactionId(order.get("id"))
                    .paymentTime(LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            log.info(
                    "Payment initiated successfully: bookingId={}, paymentId={}, transactionId={}",
                    savedPayment.getBookingId(),
                    savedPayment.getId(),
                    savedPayment.getTransactionId()
            );

        } catch (RazorpayException | ArithmeticException exception) {

            log.error(
                    "Payment initiation failed: bookingId={}, userId={}",
                    event.bookingId(),
                    event.userId(),
                    exception
            );

            throw new RuntimeException(
                    "Failed to initiate payment for booking "
                            + event.bookingId(),
                    exception
            );
        }
    }
}