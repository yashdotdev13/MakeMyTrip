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
import com.company.MakeMyTrip.booking_service.entity.IdempotencyRecord;
import com.company.MakeMyTrip.booking_service.enums.BookingStatus;
import com.company.MakeMyTrip.booking_service.exceptions.BookingModificationNotAllowedException;
import com.company.MakeMyTrip.booking_service.exceptions.BookingNotFoundException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidBookingStateException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidUserContextException;
import com.company.MakeMyTrip.booking_service.repository.BookingRepository;
import com.company.MakeMyTrip.booking_service.repository.IdempotencyRecordRepository;
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
    private final IdempotencyRecordRepository idempotencyRecordRepository;

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

        if(booking.getStatus() != BookingStatus.PENDING){
            throw new BookingModificationNotAllowedException(
                    "Booking cannot be modified from status "+ booking.getStatus()
            );
        }
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

        log.info("Confirming booking bookingId={} userId={} idempotencyKey={}",
                request.getBookingId(), userId, request.getIdempotencyKey());

        IdempotencyRecord existingRecord = idempotencyRecordRepository.findByUserIdAndIdempotencyKey(userId, request.getIdempotencyKey())
                .orElse(null);
        if (existingRecord != null) {
            if (!existingRecord.getBookingId().equals(request.getBookingId())) {
                throw new InvalidBookingStateException("Idempotency key has already been used for another booking");
            }
            Booking existingBooking = bookingRepository.findByIdAndUserId(existingRecord
                    .getBookingId(),
                    userId).orElseThrow(() -> new BookingNotFoundException("Booking not found with ID "
                    + existingRecord.getBookingId()));

            log.info("Duplicate confirmation request detected " + "bookingId={} idempotencyKey={}",
                    existingBooking.getId(), request.getIdempotencyKey());
            return BookingConfirmationResponse.builder().bookingId(existingBooking.getId())
                    .status(existingBooking
                            .getStatus().name())
                    .finalPrice(existingBooking.getAmount())
                    .message("Request already processed").build();
        }
        Booking booking = bookingRepository.findByIdAndUserId(request
                .getBookingId(), userId).orElseThrow(()
                -> new BookingNotFoundException("Booking not found with ID " + request.getBookingId()));

        /*
         * Step 3: Validate the booking state.
         *
         * Confirmation currently means:
         *
         * PENDING -> AWAITING_PAYMENT
         *
         * The state machine is the single source of truth
         * for whether this transition is allowed.
         */
        bookingStateMachine.validateTransition(booking.getStatus(), BookingStatus.AWAITING_PAYMENT);

        /*
         * Step 4: Check whether the quoted price has changed.
         *
         * Pricing, inventory and payment orchestration will
         * be implemented in later phases.
         *
         * For now, if the quoted price differs from the
         * current booking amount, update the booking amount
         * and ask the client to review the new price.
         */
        if (request.getQuotedPrice() != null && booking.getAmount() != null &&
                booking.getAmount().compareTo(request.getQuotedPrice()) != 0) {

            booking.setAmount(request.getQuotedPrice());
            booking.setUpdatedAt(LocalDateTime.now());

            Booking updatedBooking = bookingRepository.save(booking);

            log.info("Booking price updated bookingId={} oldPrice={} newPrice={}",
                    updatedBooking.getId(), booking.getAmount(), updatedBooking.getAmount());

            return BookingConfirmationResponse.builder().bookingId(updatedBooking.getId())
                    .status(updatedBooking.getStatus().name())
                    .finalPrice(updatedBooking.getAmount())
                    .message("Price has changed. Please review the new price.").build();
        }

        /*
         * Step 5: Move booking to AWAITING_PAYMENT.
         */
        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        /*
         * Step 6: Store the idempotency record only after
         * the booking transition has been successfully persisted.
         */
        IdempotencyRecord idempotencyRecord = IdempotencyRecord.builder()
                .userId(userId).idempotencyKey(request
                        .getIdempotencyKey())
                .bookingId(savedBooking.getId()).build();

        idempotencyRecordRepository.save(idempotencyRecord);

        log.info("Booking moved to AWAITING_PAYMENT " + "bookingId={} userId={} idempotencyKey={}",
                savedBooking.getId(), userId, request.getIdempotencyKey());
        return BookingConfirmationResponse.builder()
                .bookingId(savedBooking.getId())
                .status(savedBooking.getStatus()
                        .name()).finalPrice(savedBooking.getAmount())
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