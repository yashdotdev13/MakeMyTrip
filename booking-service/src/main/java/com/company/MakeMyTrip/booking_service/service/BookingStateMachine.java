package com.company.MakeMyTrip.booking_service.service;

import com.company.MakeMyTrip.booking_service.enums.BookingStatus;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidBookingStateException;
import org.springframework.stereotype.Component;

@Component
public class BookingStateMachine {

    public void validateTransition(
            BookingStatus currentStatus,
            BookingStatus targetStatus) {

        if (currentStatus == null || targetStatus == null) {
            throw new InvalidBookingStateException(
                    "Booking status cannot be null"
            );
        }

        if (currentStatus == targetStatus) {
            throw new InvalidBookingStateException(
                    "Booking is already in status " + currentStatus
            );
        }

        boolean allowed = switch (currentStatus) {

            case PENDING ->
                    targetStatus == BookingStatus.AWAITING_PAYMENT
                            || targetStatus == BookingStatus.CANCELLED;

            case AWAITING_PAYMENT ->
                    targetStatus == BookingStatus.CONFIRMED
                            || targetStatus == BookingStatus.CANCELLED;

            case CONFIRMED ->
                    targetStatus == BookingStatus.CANCELLED;

            case CANCELLED -> false;
        };

        if (!allowed) {
            throw new InvalidBookingStateException(
                    "Invalid booking state transition: "
                            + currentStatus
                            + " -> "
                            + targetStatus
            );
        }
    }
}