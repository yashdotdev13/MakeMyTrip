package com.company.MakeMyTrip.booking_service.repository;

import com.company.MakeMyTrip.booking_service.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );
}