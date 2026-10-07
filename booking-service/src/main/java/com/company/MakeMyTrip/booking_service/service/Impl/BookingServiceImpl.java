package com.company.MakeMyTrip.booking_service.service.Impl;

import com.company.MakeMyTrip.booking_service.auth.UserContextHolder;
import com.company.MakeMyTrip.booking_service.dtos.BookingConfirmationRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingConfirmationResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingCountResponse;
import com.company.MakeMyTrip.booking_service.dtos.BookingRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingResponse;
import com.company.MakeMyTrip.booking_service.entity.Booking;
import com.company.MakeMyTrip.booking_service.entity.IdempotencyRecord;
import com.company.MakeMyTrip.booking_service.entity.Reservation;
import com.company.MakeMyTrip.booking_service.enums.BookingStatus;
import com.company.MakeMyTrip.booking_service.exceptions.BookingModificationNotAllowedException;
import com.company.MakeMyTrip.booking_service.exceptions.BookingNotFoundException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidBookingStateException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidUserContextException;
import com.company.MakeMyTrip.booking_service.kafka.BookingDemandEventPublisher;
import com.company.MakeMyTrip.booking_service.kafka.PaymentRequestedEventPublisher;
import com.company.MakeMyTrip.booking_service.repository.BookingRepository;
import com.company.MakeMyTrip.booking_service.repository.IdempotencyRecordRepository;
import com.company.MakeMyTrip.booking_service.service.BookingService;
import com.company.MakeMyTrip.booking_service.service.ReservationService;

import com.company.MakeMyTrip.events.BookingDemandEventType;
import com.company.MakeMyTrip.events.PaymentCompletedEvent;
import com.company.MakeMyTrip.events.PaymentFailedEvent;
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
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ReservationService reservationService;
    private final BookingDemandEventPublisher bookingDemandEventPublisher;
    private final PaymentRequestedEventPublisher paymentRequestedEventPublisher;

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

        Booking savedBooking = bookingRepository.save(booking);

        bookingDemandEventPublisher.publish(
                savedBooking.getId(),
                savedBooking.getReferenceId(),
                savedBooking.getBookingType().name(),
                1,
                savedBooking.getTravelDate().toString(),
                BookingDemandEventType.BOOKING_CREATED
        );

        log.info(
                "Booking created successfully bookingId={} userId={}",
                savedBooking.getId(),
                userId
        );

        return toResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {

        Long userId = getRequiredUserId();

        log.info(
                "Fetching booking bookingId={} userId={}",
                bookingId,
                userId
        );

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID " + bookingId
                        )
                );

        return toResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingByUser() {

        Long userId = getRequiredUserId();

        log.info("Fetching all bookings for userId={}", userId);

        return bookingRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BookingResponse updateBooking(
            Long bookingId,
            BookingRequest request
    ) {

        Long userId = getRequiredUserId();

        log.info(
                "Updating booking bookingId={} userId={}",
                bookingId,
                userId
        );

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID " + bookingId
                        )
                );

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingModificationNotAllowedException(
                    "Booking cannot be modified from status "
                            + booking.getStatus()
            );
        }

        booking.setBookingType(request.getBookingType());
        booking.setReferenceId(request.getReferenceId());
        booking.setTravelDate(request.getTravelDate());
        booking.setAmount(request.getAmount());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(booking);

        log.info(
                "Booking updated successfully bookingId={}",
                bookingId
        );

        return toResponse(updatedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {

        Long userId = getRequiredUserId();

        log.info(
                "Cancelling booking bookingId={} userId={}",
                bookingId,
                userId
        );

        Booking booking = bookingRepository.findByIdAndUserId(
                        bookingId,
                        userId
                )
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID " + bookingId
                        )
                );

        booking.cancel();

        reservationService.releaseReservation(booking.getId());

        Booking savedBooking = bookingRepository.save(booking);

        bookingDemandEventPublisher.publish(
                savedBooking.getId(),
                savedBooking.getReferenceId(),
                savedBooking.getBookingType().name(),
                1,
                savedBooking.getTravelDate().toString(),
                BookingDemandEventType.BOOKING_CANCELLED
        );

        log.info(
                "Booking cancelled successfully bookingId={}",
                bookingId
        );
    }

    @Override
    public BookingCountResponse getBookingCount(
            Long referenceId,
            String travelDate
    ) {

        log.info(
                "Fetching booking count referenceId={} travelDate={}",
                referenceId,
                travelDate
        );

        LocalDate parsedTravelDate = LocalDate.parse(travelDate);

        int count = bookingRepository.countByReferenceIdAndTravelDate(
                referenceId,
                parsedTravelDate
        );

        return BookingCountResponse.builder()
                .referenceId(referenceId)
                .travelDate(travelDate)
                .currentBookings(count)
                .build();
    }

    @Override
    @Transactional
    public BookingConfirmationResponse confirmBooking(
            BookingConfirmationRequest request
    ) {

        Long userId = getRequiredUserId();

        log.info(
                "Confirming booking bookingId={} userId={} idempotencyKey={}",
                request.getBookingId(),
                userId,
                request.getIdempotencyKey()
        );

        // Step 1: Check whether this idempotency key was already processed.
        IdempotencyRecord existingRecord =
                idempotencyRecordRepository
                        .findByUserIdAndIdempotencyKey(
                                userId,
                                request.getIdempotencyKey()
                        )
                        .orElse(null);

        if (existingRecord != null) {

            if (!existingRecord.getBookingId().equals(
                    request.getBookingId()
            )) {
                throw new InvalidBookingStateException(
                        "Idempotency key has already been used "
                                + "for another booking"
                );
            }

            Booking existingBooking =
                    bookingRepository.findByIdAndUserId(
                                    existingRecord.getBookingId(),
                                    userId
                            )
                            .orElseThrow(() ->
                                    new BookingNotFoundException(
                                            "Booking not found with ID "
                                                    + existingRecord.getBookingId()
                                    )
                            );

            log.info(
                    "Duplicate confirmation request detected " +
                            "bookingId={} idempotencyKey={}",
                    existingBooking.getId(),
                    request.getIdempotencyKey()
            );

            return BookingConfirmationResponse.builder()
                    .bookingId(existingBooking.getId())
                    .status(existingBooking.getStatus().name())
                    .finalPrice(existingBooking.getAmount())
                    .message("Request already processed")
                    .build();
        }

        // Step 2: Fetch the booking belonging to the authenticated user.
        Booking booking = bookingRepository.findByIdAndUserId(
                        request.getBookingId(),
                        userId
                )
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID "
                                        + request.getBookingId()
                        )
                );

        // Step 3: Check whether the quoted price has changed.
        if (request.getQuotedPrice() != null
                && booking.getAmount() != null
                && booking.getAmount()
                .compareTo(request.getQuotedPrice()) != 0) {

            var oldPrice = booking.getAmount();

            booking.setAmount(request.getQuotedPrice());
            booking.setUpdatedAt(LocalDateTime.now());

            Booking updatedBooking = bookingRepository.save(booking);

            log.info(
                    "Booking price updated bookingId={} oldPrice={} newPrice={}",
                    updatedBooking.getId(),
                    oldPrice,
                    updatedBooking.getAmount()
            );

            return BookingConfirmationResponse.builder()
                    .bookingId(updatedBooking.getId())
                    .status(updatedBooking.getStatus().name())
                    .finalPrice(updatedBooking.getAmount())
                    .message(
                            "Price has changed. Please review the new price."
                    )
                    .build();
        }

        // Step 4: Reserve inventory before moving the booking forward.
        Reservation reservation = reservationService.reserveInventory(
                booking.getId(),
                booking.getBookingType(),
                booking.getReferenceId(),
                booking.getTravelDate(),
                1
        );

        log.info(
                "Inventory reserved bookingId={} reservationId={}",
                booking.getId(),
                reservation.getId()
        );

        // Step 5: Move the booking to AWAITING_PAYMENT.
        // The Booking entity validates the transition.
        booking.moveToAwaitingPayment();

        Booking savedBooking = bookingRepository.save(booking);

        // Step 6: Save the idempotency record in the same transaction.
        IdempotencyRecord idempotencyRecord =
                IdempotencyRecord.builder()
                        .userId(userId)
                        .idempotencyKey(request.getIdempotencyKey())
                        .bookingId(savedBooking.getId())
                        .build();

        idempotencyRecordRepository.save(idempotencyRecord);

        paymentRequestedEventPublisher.publish(
                savedBooking.getId(),
                savedBooking.getUserId(),
                savedBooking.getAmount(),
                "RAZORPAY"
        );

        log.info(
                "Booking moved to AWAITING_PAYMENT and payment requested " +
                        "bookingId={} userId={} idempotencyKey={}",
                savedBooking.getId(),
                userId,
                request.getIdempotencyKey()
        );

        return BookingConfirmationResponse.builder()
                .bookingId(savedBooking.getId())
                .status(savedBooking.getStatus().name())
                .finalPrice(savedBooking.getAmount())
                .message("Booking is awaiting payment.")
                .build();
    }

    @Override
    public BookingResponse getBookingByIdInternal(Long bookingId) {

        log.info(
                "Fetching booking internally bookingId={}",
                bookingId
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID " + bookingId
                        )
                );

        return toResponse(booking);
    }

    @Override
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info(
                "Processing payment completed event: paymentId={}, bookingId={}, transactionId={}",
                event.paymentId(),
                event.bookingId(),
                event.transactionId()
        );

        Booking booking = bookingRepository.findById(event.bookingId())
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID " + event.bookingId()
                        )
                );

        /*
         * Kafka events can be delivered more than once.
         *
         * If the booking is already confirmed, this event has
         * effectively already been processed.
         */
        if (booking.getStatus() == BookingStatus.CONFIRMED) {

            log.info(
                    "Payment completed event already processed: bookingId={}",
                    event.bookingId()
            );

            return;
        }

        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {

            throw new InvalidBookingStateException(
                    "Booking cannot be confirmed from status "
                            + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        log.info(
                "Booking confirmed after successful payment: bookingId={}, paymentId={}, transactionId={}",
                savedBooking.getId(),
                event.paymentId(),
                event.transactionId()
        );
    }

    @Override
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.warn(
                "Processing payment failed event: bookingId={}, reason={}",
                event.bookingId(),
                event.reason()
        );

        Booking booking = bookingRepository.findById(event.bookingId())
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID " + event.bookingId()
                        )
                );

        /*
         * Kafka events can be delivered more than once.
         */
        if (booking.getStatus() == BookingStatus.PAYMENT_FAILED) {

            log.info(
                    "Payment failed event already processed: bookingId={}",
                    event.bookingId()
            );

            return;
        }

        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {

            throw new InvalidBookingStateException(
                    "Booking cannot be marked payment failed from status "
                            + booking.getStatus()
            );
        }

        /*
         * Payment failed, therefore the inventory reservation
         * created during confirmation must be released.
         */
        reservationService.releaseReservation(booking.getId());

        booking.setStatus(BookingStatus.PAYMENT_FAILED);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        log.info(
                "Booking marked as payment failed: bookingId={}, reason={}",
                savedBooking.getId(),
                event.reason()
        );
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

        return BookingResponse.builder()
                .id(booking.getId())
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