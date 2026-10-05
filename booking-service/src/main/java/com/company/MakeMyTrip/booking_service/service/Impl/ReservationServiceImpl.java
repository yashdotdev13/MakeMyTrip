package com.company.MakeMyTrip.booking_service.service.Impl;


import com.company.MakeMyTrip.booking_service.entity.Inventory;
import com.company.MakeMyTrip.booking_service.entity.Reservation;
import com.company.MakeMyTrip.booking_service.enums.BookingType;
import com.company.MakeMyTrip.booking_service.enums.ReservationStatus;
import com.company.MakeMyTrip.booking_service.exceptions.BookingNotFoundException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidBookingStateException;
import com.company.MakeMyTrip.booking_service.repository.InventoryRepository;
import com.company.MakeMyTrip.booking_service.repository.ReservationRepository;
import com.company.MakeMyTrip.booking_service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    @Value("${booking.reservation.hold-duration-minutes:10}")
    private long holdDurationMinutes;

    @Override
    @Transactional
    public Reservation reserveInventory(Long bookingId, BookingType bookingType, Long referenceId, LocalDate travelDate, int quantity) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("Invalid booking ID");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        if (holdDurationMinutes <= 0) {
            throw new IllegalStateException("Reservation hold duration must be positive");
        }
        Optional<Reservation> existingReservation = reservationRepository.findByBookingId(bookingId);

        if (existingReservation.isPresent()) {
            throw new InvalidBookingStateException("A reservation already exists for this booking");
        }

        Inventory inventory = inventoryRepository.findByBookingTypeAndReferenceIdAndTravelDate(bookingType,
                referenceId, travelDate)
                .orElseThrow(() -> new InvalidBookingStateException("Inventory not found for the requested booking"));
        int updatedRows = inventoryRepository.reserveCapacity(inventory.getId(), quantity);
        if (updatedRows == 0) {
            throw new InvalidBookingStateException("Insufficient inventory capacity");
        }
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = Reservation.builder().bookingId(bookingId).inventory(inventory)
                .quantity(quantity)
                .status(ReservationStatus.HELD)
                .expiresAt(now.plusMinutes(holdDurationMinutes))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return reservationRepository.save(reservation);
    }


    @Override
    @Transactional
    public Reservation confirmReservation(Long bookingId) {
        Reservation reservation = reservationRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Reservation not found for booking: " + bookingId));

        Reservation lockedReservation = reservationRepository.findByIdForUpdate(reservation.getId())
                .orElseThrow(() -> new BookingNotFoundException("Reservation not found: " + reservation.getId()));

        if (lockedReservation.getStatus() == ReservationStatus.CONFIRMED) {
            return lockedReservation;
        }
        if (lockedReservation.getStatus() != ReservationStatus.HELD) {
            throw new InvalidBookingStateException("Only HELD reservations can be confirmed");
        }
        LocalDateTime now = LocalDateTime.now();

        if (!lockedReservation.getExpiresAt().isAfter(now)) {
            throw new InvalidBookingStateException("Reservation has expired");
        }
        int updatedRows = inventoryRepository.confirmCapacity(lockedReservation.getInventory().getId(), lockedReservation.getQuantity());

        if (updatedRows == 0) {
            throw new IllegalStateException("Unable to transfer reserved inventory to confirmed inventory");
        }
        lockedReservation.setStatus(ReservationStatus.CONFIRMED);
        lockedReservation.setUpdatedAt(now);

        return reservationRepository.save(lockedReservation);
    }

    @Override
    @Transactional
    public Optional<Reservation> releaseReservation(Long bookingId) {

        Reservation reservation = reservationRepository
                .findByBookingId(bookingId)
                .orElse(null);

        if (reservation == null) {
            log.debug(
                    "No reservation found for bookingId={}, nothing to release",
                    bookingId
            );

            return Optional.empty();
        }

        Reservation lockedReservation =
                reservationRepository.findByIdForUpdate(reservation.getId())
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        "Reservation not found: "
                                                + reservation.getId()
                                )
                        );

        if (lockedReservation.getStatus() == ReservationStatus.RELEASED) {
            return Optional.of(lockedReservation);
        }

        if (lockedReservation.getStatus() == ReservationStatus.EXPIRED) {
            return Optional.of(lockedReservation);
        }

        Inventory inventory = lockedReservation.getInventory();
        int quantity = lockedReservation.getQuantity();

        int updatedRows;

        if (lockedReservation.getStatus() == ReservationStatus.HELD) {

            updatedRows = inventoryRepository.releaseReservedCapacity(
                    inventory.getId(),
                    quantity
            );

        } else if (lockedReservation.getStatus() == ReservationStatus.CONFIRMED) {

            updatedRows = inventoryRepository.releaseConfirmedCapacity(
                    inventory.getId(),
                    quantity
            );

        } else {

            throw new InvalidBookingStateException(
                    "Reservation cannot be released from its current state"
            );
        }

        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "Unable to release inventory capacity"
            );
        }

        lockedReservation.setStatus(ReservationStatus.RELEASED);
        lockedReservation.setUpdatedAt(LocalDateTime.now());

        Reservation releasedReservation =
                reservationRepository.save(lockedReservation);

        return Optional.of(releasedReservation);
    }

    @Override
    @Transactional
    public int expireDueReservations() {

        LocalDateTime now = LocalDateTime.now();

        List<Long> candidateIds =
                reservationRepository.findExpiredReservationIds(
                        ReservationStatus.HELD,
                        now,
                        PageRequest.of(0, 100)
                );
        int expiredCount = 0;
        for (Long reservationId : candidateIds) {
            Reservation reservation = reservationRepository.findByIdForUpdate(reservationId).orElse(null);

            if (reservation == null) {
                continue;
            }
            // Recheck after acquiring the row lock.
            if (reservation.getStatus() != ReservationStatus.HELD || reservation.getExpiresAt().isAfter(now)) {
                continue;
            }
            int updatedRows = inventoryRepository.releaseReservedCapacity(reservation.getInventory().getId(), reservation.getQuantity());

            if (updatedRows == 0) {
                throw new IllegalStateException("Unable to release inventory for reservation: " + reservationId);
            }
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservation.setUpdatedAt(now);

            reservationRepository.save(reservation);
            expiredCount++;
        }
        return expiredCount;
    }
}
