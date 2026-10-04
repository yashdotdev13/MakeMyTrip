package com.company.MakeMyTrip.booking_service.scheduler;

import com.company.MakeMyTrip.booking_service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryScheduler {

    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "${booking.reservation.expiry-interval-ms:30000}")
    public void expireReservations() {
        int expiredCount = reservationService.expireDueReservations();

        if (expiredCount > 0) {
            log.info(
                    "Expired {} reservations and released their inventory",
                    expiredCount
            );
        }
    }
}