package com.company.MakeMyTrip.booking_service.service.Impl;

import com.company.MakeMyTrip.booking_service.auth.UserContextHolder;
import com.company.MakeMyTrip.booking_service.dtos.BookingRequest;
import com.company.MakeMyTrip.booking_service.dtos.BookingResponse;
import com.company.MakeMyTrip.booking_service.entity.Booking;
import com.company.MakeMyTrip.booking_service.repository.BookingRepository;
import com.company.MakeMyTrip.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;


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
        return null;
    }

    @Override
    public List<BookingResponse> getBookingByUser() {
        return List.of();
    }

    @Override
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        return null;
    }

    @Override
    public void cancelBooking(Long bookingId) {

    }
}
