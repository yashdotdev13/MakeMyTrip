package com.company.MakeMyTrip.pricing_service.repository;

import com.company.MakeMyTrip.pricing_service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, Long> {

    boolean existsByEventId(String eventId);
}