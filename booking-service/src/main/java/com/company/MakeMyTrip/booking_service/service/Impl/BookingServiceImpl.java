package com.company.MakeMyTrip.booking_service.service.Impl;

import com.company.MakeMyTrip.booking_service.auth.UserContextHolder;
import com.company.MakeMyTrip.booking_service.client.NotificationClient;
import com.company.MakeMyTrip.booking_service.client.PaymentClient;
import com.company.MakeMyTrip.booking_service.client.PricingClient;
import com.company.MakeMyTrip.booking_service.dtos.*;
import com.company.MakeMyTrip.booking_service.entity.Booking;
import com.company.MakeMyTrip.booking_service.enums.BookingStatus;
import com.company.MakeMyTrip.booking_service.repository.BookingRepository;
import com.company.MakeMyTrip.booking_service.service.BookingService;
import jakarta.transaction.Transactional;
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
    private final PricingClient pricingClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Creating booking for userId: {}", userId);

        Booking booking = modelMapper.map(request, Booking.class);
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with bookingId: {}", savedBooking.getId());

        return modelMapper.map(savedBooking, BookingResponse.class);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Fetching booking with id: {} for userId: {}", bookingId, userId);

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID " + bookingId + " for current user"));

        return modelMapper.map(booking, BookingResponse.class);
    }

    @Override
    public List<BookingResponse> getBookingByUser() {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Fetching all bookings for userId: {}", userId);

        List<Booking> bookings = bookingRepository.findAllByUserId(userId);

        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Updating booking with ID:{} for userId: {}", bookingId, userId);

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        booking.setBookingType(request.getBookingType());
        booking.setReferenceId(request.getReferenceId());
        booking.setAmount(request.getAmount());

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking {} updated successfully", bookingId);

        return modelMapper.map(updatedBooking, BookingResponse.class);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Cancelling booking with ID: {} for userId: {}", bookingId, userId);

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID " + bookingId));

        bookingRepository.delete(booking);
        log.info("Booking cancelled successfully for bookingId: {}", bookingId);
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
    @Transactional
    public BookingConfirmationResponse confirmBooking(BookingConfirmationRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Confirming booking {} for user {}", request.getBookingId(), userId);

        Booking booking = bookingRepository.findByIdAndUserId(request.getBookingId(), userId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID " + request.getBookingId()));

        if (!booking.getStatus().equals(BookingStatus.PENDING)) {
            return BookingConfirmationResponse.builder()
                    .bookingId(booking.getId())
                    .status(booking.getStatus().name())
                    .finalPrice(booking.getAmount())
                    .message("Booking cannot be confirmed. Current status: " + booking.getStatus())
                    .build();
        }

        PriceQuoteRequest priceRequest = PriceQuoteRequest.builder()
                .referenceId(booking.getReferenceId())
                .bookingType(booking.getBookingType().name())
                .userId(userId)
                .quantity(1)
                .travelDate(booking.getTravelDate().toString())
                .build();

        PriceQuoteResponse quote = pricingClient.getPriceQuote(priceRequest).getData();
        if (quote == null || quote.getAdjustedPrice() == null) {
            throw new RuntimeException("Pricing service returned null adjusted price for referenceId " + booking.getReferenceId());
        }

        Double currentPrice = quote.getAdjustedPrice();


        if (!currentPrice.equals(request.getQuotedPrice())) {
            booking.setAmount(currentPrice);
            bookingRepository.save(booking);

            return BookingConfirmationResponse.builder()
                    .bookingId(booking.getId())
                    .status(booking.getStatus().name())
                    .finalPrice(currentPrice)
                    .message("Price has changed. Please review new price.")
                    .build();
        }

        BookingCountResponse countResponse = getBookingCount(booking.getReferenceId(), booking.getTravelDate().toString());
        int maxCapacity = 100;
        if (countResponse.getCurrentBookings() >= maxCapacity) {
            return BookingConfirmationResponse.builder()
                    .bookingId(booking.getId())
                    .status(booking.getStatus().name())
                    .finalPrice(currentPrice)
                    .message("No availability for selected travel date.")
                    .build();
        }

        pricingClient.lockPrice(booking.getReferenceId(), booking.getBookingType().name());

        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setAmount(currentPrice);
        bookingRepository.save(booking);

        log.info("Booking {} confirmed. Status set to AWAITING_PAYMENT", booking.getId());

        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .bookingId(booking.getId())
                    .amount(booking.getAmount())
                    .build();

            PaymentResponse paymentResponse = paymentClient.initiatePayment(paymentRequest);
            log.info("Payment initiated for bookingId={} | Razorpay TxnID={}", booking.getId(), paymentResponse.getTransactionId());
        } catch (Exception e) {
            log.error("Payment initiation failed for bookingId={}: {}", booking.getId(), e.getMessage());
        }

        try {
            EmailRequest emailRequest = EmailRequest.builder()
                    .to("useremail@example.com")
                    .subject("Booking Confirmation - MakeMyTrip")
                    .body(String.format(
                            "Hello!\n\nYour booking #%d to referenceId %d has been successfully confirmed.\nTotal Amount: ₹%.2f\nTravel Date: %s\n\nThank you for booking with MakeMyTrip!",
                            booking.getId(),
                            booking.getReferenceId(),
                            booking.getAmount(),
                            booking.getTravelDate()
                    ))
                    .build();

            notificationClient.sendEmail(emailRequest);
            log.info("Booking confirmation email sent for bookingId={}", booking.getId());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for bookingId={}: {}", booking.getId(), e.getMessage());
        }

        return BookingConfirmationResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus().name())
                .finalPrice(currentPrice)
                .message("Booking confirmed. Payment initiated. Confirmation email sent.")
                .build();
    }

    @Override
    public BookingResponse getBookingByIdInternal(Long bookingId) {
        log.info("Fetching booking internally with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID " + bookingId));

        return modelMapper.map(booking, BookingResponse.class);
    }
}
