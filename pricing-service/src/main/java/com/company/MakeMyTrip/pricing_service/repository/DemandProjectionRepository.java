package com.company.MakeMyTrip.pricing_service.repository;

import com.company.MakeMyTrip.pricing_service.entity.DemandProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DemandProjectionRepository
        extends JpaRepository<DemandProjection, Long> {

    Optional<DemandProjection>
    findByReferenceIdAndBookingTypeAndTravelDate(
            Long referenceId,
            String bookingType,
            LocalDate travelDate
    );
}