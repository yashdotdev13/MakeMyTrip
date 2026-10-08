package com.company.MakeMyTrip.notification_service.service;

import com.company.MakeMyTrip.events.PaymentCompletedEvent;
import com.company.MakeMyTrip.events.PaymentFailedEvent;
import com.company.MakeMyTrip.notification_service.dtos.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationContactStore notificationContactStore;

    public void sendEmail(EmailRequest request) {

        log.info(
                "Sending email: to={}, subject={}",
                request.getTo(),
                request.getSubject()
        );

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());

        javaMailSender.send(message);

        log.info(
                "Email sent successfully: to={}, subject={}",
                request.getTo(),
                request.getSubject()
        );
    }

    public void sendPaymentCompletedEmail(PaymentCompletedEvent event) {

        log.info(
                "Preparing payment completed notification: bookingId={}, paymentId={}",
                event.bookingId(),
                event.paymentId()
        );

        Optional<String> recipient = resolveRecipient(event.userId());

        if (recipient.isEmpty()) {
            log.warn(
                    "Skipping payment completed notification: no email found for userId={}",
                    event.userId()
            );
            return;
        }

        EmailRequest request = EmailRequest.builder()
                .to(recipient.get())
                .subject("Payment Successful - MakeMyTrip")
                .body(
                        "Your payment was completed successfully.\n\n"
                                + "Booking ID: " + event.bookingId() + "\n"
                                + "Payment ID: " + event.paymentId() + "\n"
                                + "Amount: ₹" + event.amount() + "\n"
                                + "Transaction ID: " + event.transactionId() + "\n\n"
                                + "Thank you for using MakeMyTrip."
                )
                .build();

        sendEmail(request);
    }

    public void sendPaymentFailedEmail(PaymentFailedEvent event) {

        log.info(
                "Preparing payment failed notification: bookingId={}",
                event.bookingId()
        );

        Optional<String> recipient = resolveRecipient(event.userId());

        if (recipient.isEmpty()) {
            log.warn(
                    "Skipping payment failed notification: no email found for userId={}",
                    event.userId()
            );
            return;
        }

        EmailRequest request = EmailRequest.builder()
                .to(recipient.get())
                .subject("Payment Failed - MakeMyTrip")
                .body(
                        "Your payment could not be completed.\n\n"
                                + "Booking ID: " + event.bookingId() + "\n"
                                + "Amount: ₹" + event.amount() + "\n"
                                + "Reason: " + event.reason() + "\n\n"
                                + "Please try again or contact support."
                )
                .build();

        sendEmail(request);
    }

    private Optional<String> resolveRecipient(Long userId) {

        return Optional.ofNullable(
                notificationContactStore.getEmail(userId)
        );
    }
}