package com.company.MakeMyTrip.pricing_service.repository;

import com.company.MakeMyTrip.pricing_service.entity.PriceLock;
import com.company.MakeMyTrip.pricing_service.enums.PriceLockStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceLockRepository
        extends JpaRepository<PriceLock, Long> {

    Optional<PriceLock> findByReferenceIdAndBookingTypeAndUserIdAndStatus(
            Long referenceId,
            String bookingType,
            Long userId,
            PriceLockStatus status
    );

    List<PriceLock> findByStatusAndValidTillBefore(
            PriceLockStatus status,
            LocalDateTime time
    );
}