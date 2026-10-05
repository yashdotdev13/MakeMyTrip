package com.company.MakeMyTrip.pricing_service.service;

import com.company.MakeMyTrip.pricing_service.dtos.BasePriceRequest;
import com.company.MakeMyTrip.pricing_service.dtos.BasePriceResponse;
import com.company.MakeMyTrip.pricing_service.entity.BasePrice;

public interface BasePriceService {

    BasePrice getBasePrice(
            Long referenceId,
            String bookingType
    );

    BasePriceResponse createBasePrice(
            BasePriceRequest request
    );

    BasePriceResponse updateBasePrice(
            Long basePriceId,
            BasePriceRequest request
    );

    BasePriceResponse getBasePriceById(
            Long basePriceId
    );

    void deactivateBasePrice(
            Long basePriceId
    );
}