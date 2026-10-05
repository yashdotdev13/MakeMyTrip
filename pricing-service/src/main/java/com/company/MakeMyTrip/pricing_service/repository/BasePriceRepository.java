package com.company.MakeMyTrip.pricing_service.repository;

import com.company.MakeMyTrip.pricing_service.entity.BasePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasePriceRepository
        extends JpaRepository<BasePrice, Long> {

    Optional<BasePrice> findByReferenceIdAndBookingTypeAndActiveTrue(
            Long referenceId,
            String bookingType
    );

    boolean existsByReferenceIdAndBookingTypeAndActiveTrue(
            Long referenceId,
            String bookingType
    );
}