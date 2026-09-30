package com.company.MakeMyTrip.booking_service.service.Impl;

import com.company.MakeMyTrip.booking_service.auth.UserContextHolder;
import com.company.MakeMyTrip.booking_service.dtos.BookingConfirmationRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingConfirmationResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingCountResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingResponse;
import com.company.MakeMyTrip.booking_service.dtos.PriceQuoteRequest;
import com.company.MakeMyTrip.booking_service.dtos.PriceQuoteResponse;
import com.company.MakeMyTrip.booking_service.dtos.PriceLockRequest;
import com.company.MakeMyTrip.booking_service.dtos.PriceLockResponse;
import com.company.MakeMyTrip.booking_service.entity.Booking;
import com.company.MakeMyTrip.booking_service.enums.BookingStatus;
import com.company.MakeMyTrip.booking_service.exceptions.BookingNotFoundException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidUserContextException;
import com.company.MakeMyTrip.booking_service.repository.BookingRepository;
import com.company.MakeMyTrip.booking_service.service.BookingService;
import com.company.MakeMyTrip.booking_service.service.BookingStateMachine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingStateMachine bookingStateMachine;

    @Override
    public BookingResponse createBooking(BookingRequest request) {

        Long userId = getRequiredUserId();
        log.info("Creating booking for userId={}", userId);
        Booking booking = new Booking();

        booking.setUserId(userId);
        booking.setBookingType(request.getBookingType());
        booking.setReferenceId(request.getReferenceId());
        booking.setTravelDate(request.getTravelDate());
        booking.setAmount(request.getAmount());
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully bookingId={} userId={}", savedBooking.getId(), userId);
        return toResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Long userId = getRequiredUserId();
        log.info("Fetching booking bookingId={} userId={}", bookingId, userId);
        Booking booking = bookingRepository.findByIdAndUserId(bookingId,
                userId).orElseThrow(() ->
                new BookingNotFoundException("Booking not found with ID " + bookingId));
        return toResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingByUser() {
        Long userId = getRequiredUserId();
        log.info("Fetching all bookings for userId={}", userId);
        return bookingRepository.findAllByUserId(userId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {

        Long userId = getRequiredUserId();
        log.info("Updating booking bookingId={} userId={}", bookingId, userId);
        Booking booking = bookingRepository.findByIdAndUserId(bookingId,
                userId).orElseThrow(() ->
                new BookingNotFoundException("Booking not found with ID " + bookingId));

        booking.setBookingType(request.getBookingType());
        booking.setReferenceId(request.getReferenceId());
        booking.setTravelDate(request.getTravelDate());
        booking.setAmount(request.getAmount());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking updated successfully bookingId={}", bookingId);
        return toResponse(updatedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {

        Long userId = getRequiredUserId();
        log.info("Cancelling booking bookingId={} userId={}", bookingId, userId);
        Booking booking = bookingRepository.findByIdAndUserId(bookingId,
                userId).orElseThrow(()
                -> new BookingNotFoundException("Booking not found with ID " + bookingId));

        bookingStateMachine.validateTransition(
                booking.getStatus(),
                BookingStatus.CANCELLED
        );
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
        log.info("Booking cancelled successfully bookingId={}", bookingId);
    }

    @Override
    public BookingCountResponse getBookingCount(Long referenceId, String travelDate) {

        log.info("Fetching booking count referenceId={} travelDate={}", referenceId, travelDate);
        LocalDate parsedTravelDate = LocalDate.parse(travelDate);
        int count = bookingRepository.countByReferenceIdAndTravelDate(referenceId, parsedTravelDate);
        return BookingCountResponse.builder().referenceId(referenceId).
                travelDate(travelDate).currentBookings(count).build();
    }

    @Override
    @Transactional
    public BookingConfirmationResponse confirmBooking(BookingConfirmationRequest request) {

        Long userId = getRequiredUserId();
        log.info("Confirming booking bookingId={} userId={}", request.getBookingId(), userId);
        Booking booking = bookingRepository.findByIdAndUserId(request
                .getBookingId(), userId).orElseThrow(()
                -> new RuntimeException("Booking not found with ID " + request.getBookingId()));

        if (booking.getStatus() != BookingStatus.PENDING) {
            return BookingConfirmationResponse
                    .builder()
                    .bookingId(booking
                    .getId())
                    .status(booking.getStatus().name())
                    .finalPrice(booking.getAmount())
                    .message("Booking cannot be confirmed. Current status: " + booking.getStatus()).build();
        }
        /*
         * Pricing, inventory, payment and notification
         * orchestration will be implemented in later phases.
         *
         * For Phase 1, confirmation only moves the booking
         * into the AWAITING_PAYMENT state.
         */

        if (request.getQuotedPrice() != null && booking.getAmount() != null
                && booking.getAmount().compareTo(request.getQuotedPrice()) != 0) {

            booking.setAmount(request.getQuotedPrice());
            booking.setUpdatedAt(LocalDateTime.now());
            Booking updatedBooking = bookingRepository.save(booking);
            log.info("Booking price updated bookingId={}", updatedBooking.getId());

            return BookingConfirmationResponse.builder().bookingId(updatedBooking
                    .getId()).status(updatedBooking.getStatus().name())
                    .finalPrice(updatedBooking.getAmount())
                    .message("Price has changed. Please review the new price.").build();
        }

        bookingStateMachine.validateTransition(
                booking.getStatus(),
                BookingStatus.AWAITING_PAYMENT
        );
        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking moved to AWAITING_PAYMENT bookingId={}", savedBooking.getId());
        return BookingConfirmationResponse.builder().bookingId(savedBooking.getId())
                .status(savedBooking.getStatus().name())
                .finalPrice(savedBooking.getAmount())
                .message("Booking is awaiting payment.").build();
    }

    @Override
    public BookingResponse getBookingByIdInternal(Long bookingId) {
        log.info("Fetching booking internally bookingId={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()
                -> new RuntimeException("Booking not found with ID " + bookingId));
        return toResponse(booking);
    }


    private Long getRequiredUserId() {

        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new InvalidUserContextException(
                    "User identity is missing from the request context"
            );
        }
        return userId;
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder().id(booking.getId())
                .userId(booking.getUserId())
                .bookingType(booking.getBookingType())
                .referenceId(booking.getReferenceId())
                .status(booking.getStatus())
                .bookingDate(booking.getBookingDate())
                .travelDate(booking.getTravelDate())
                .amount(booking.getAmount())
                .build();
    }
}