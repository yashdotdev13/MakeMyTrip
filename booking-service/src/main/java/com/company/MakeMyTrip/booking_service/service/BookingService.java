package com.company.MakeMyTrip.booking_service.service;

import com.company.MakeMyTrip.booking_service.dtos.*;
import com.company.MakeMyTrip.events.PaymentCompletedEvent;
import com.company.MakeMyTrip.events.PaymentFailedEvent;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingById(Long bookingId);

    List<BookingResponse> getBookingByUser();

    BookingResponse updateBooking(Long bookingId, BookingRequest request);

    void cancelBooking(Long bookingId);

    BookingCountResponse getBookingCount(Long referenceId, String travelDate);


    BookingConfirmationResponse confirmBooking(BookingConfirmationRequest request);

    BookingResponse getBookingByIdInternal(Long bookingId);

    void handlePaymentCompleted(PaymentCompletedEvent event);

    void handlePaymentFailed(PaymentFailedEvent event);


}
