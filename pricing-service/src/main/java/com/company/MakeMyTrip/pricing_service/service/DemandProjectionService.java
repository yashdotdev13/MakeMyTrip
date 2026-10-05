package com.company.MakeMyTrip.pricing_service.service;

import com.company.MakeMyTrip.events.BookingDemandEvent;

public interface DemandProjectionService {

    void processBookingDemandEvent(
            BookingDemandEvent event
    );

}