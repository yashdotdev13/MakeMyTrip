package com.company.MakeMyTrip.pricing_service.repository;

import com.company.MakeMyTrip.pricing_service.entity.PriceLock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceLockRepository extends JpaRepository<PriceLock,Long> {

    // check if the user already has a lock for a specific booking reference
    Optional<PriceLock> findByReferenceIdAndBookingTypeAndUserId(Long referenceId,String bookingType, Long userId);


    // delete expired locks
    void deleteByValidTillBefore(java.time.LocalDateTime now);
}
