package com.company.MakeMyTrip.booking_service.service.Impl;

import com.company.MakeMyTrip.booking_service.auth.UserContextHolder;
import com.company.MakeMyTrip.booking_service.dtos.*;
import com.company.MakeMyTrip.booking_service.entity.Booking;
import com.company.MakeMyTrip.booking_service.repository.BookingRepository;
import com.company.MakeMyTrip.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;


    @Override
    public BookingResponse createBooking(BookingRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();

        log.info("Creating booking for userId:{}", userId);

        Booking booking = modelMapper.map(request, Booking.class);
        booking.setUserId(userId);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking created successfully with bookingId: {}", savedBooking.getId());

        // TODO:  send booking confirmation message/email
        // TODO : integrate payment processing here

        return modelMapper.map(savedBooking, BookingResponse.class);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Fetching booking with id: {} for userId: {}",bookingId, userId);

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found with ID " + bookingId + " for the current user"));

        return modelMapper.map(booking, BookingResponse.class);
    }


    @Override
    public List<BookingResponse> getBookingByUser() {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Fetching all bookings for userId: {}",userId);

        List<Booking> bookings = bookingRepository.findAllByUserId(userId);

        return bookings.stream()
                .map(booking-> modelMapper.map(booking, BookingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Updating booking with ID:{} for userId: {}", bookingId, userId);

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(()->new RuntimeException("Booking not found with ID: " +bookingId+ " for the current User"));

        booking.setBookingType(request.getBookingType());
        booking.setReferenceId(request.getReferenceId());
        booking.setAmount(request.getAmount());

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking {} updated successfully", bookingId);

        // TODO: send update notifications

        return modelMapper.map(updatedBooking, BookingResponse.class);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Cancelling booking with ID: {} fpr userId: {}",bookingId, userId);

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found with ID " + bookingId + " for the current user"));

        bookingRepository.delete(booking);

        log.info("Booking cancelled successfully for bookingId: {}",bookingId);

        // TODO: send cancellations notifications
        // TODO: refund payment if applicable

    }

    @Override
    public BookingCountResponse getBookingCount(Long referenceId, String travelDate) {
        log.info("Fetching booking count for referenceId={}, travelDate={}", referenceId, travelDate);
        int count = bookingRepository.countByReferenceIdAndTravelDate(referenceId, LocalDate.parse(travelDate));
        return BookingCountResponse.builder()
                .referenceId(referenceId)
                .travelDate(travelDate)
                .currentBookings(count)
                .build();
    }

    @Override
    public BookingConfirmationResponse confirmBooking(BookingConfirmationRequest request) {
        return null;
    }
}
