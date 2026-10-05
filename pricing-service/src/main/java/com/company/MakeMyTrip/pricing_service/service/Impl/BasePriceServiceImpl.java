package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.pricing_service.dtos.BasePriceRequest;
import com.company.MakeMyTrip.pricing_service.dtos.BasePriceResponse;
import com.company.MakeMyTrip.pricing_service.entity.BasePrice;
import com.company.MakeMyTrip.pricing_service.exceptions.BasePriceAlreadyExistsException;
import com.company.MakeMyTrip.pricing_service.exceptions.BasePriceNotFoundException;
import com.company.MakeMyTrip.pricing_service.repository.BasePriceRepository;
import com.company.MakeMyTrip.pricing_service.service.BasePriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasePriceServiceImpl implements BasePriceService {

    private final BasePriceRepository basePriceRepository;

    @Override
    @Transactional(readOnly = true)
    public BasePrice getBasePrice(Long referenceId, String bookingType) {
        log.debug("Fetching active base price: referenceId={}, bookingType={}", referenceId, bookingType);
        return basePriceRepository.findByReferenceIdAndBookingTypeAndActiveTrue(
                referenceId, bookingType).orElseThrow(() ->
                new BasePriceNotFoundException("Active base price not found for referenceId=" +
                        referenceId + ", bookingType=" + bookingType));
    }

    @Override
    @Transactional
    public BasePriceResponse createBasePrice(BasePriceRequest request) {
        String bookingType = request.getBookingType().trim().toUpperCase();
        log.info("Creating base price: referenceId={}, bookingType={}, price={}, currency={}",
                request.getReferenceId(), bookingType, request.getPrice(), request.getCurrency());

        boolean activePriceExists = basePriceRepository
                .existsByReferenceIdAndBookingTypeAndActiveTrue(request.getReferenceId(), bookingType);
        if (activePriceExists) {
            throw new BasePriceAlreadyExistsException("An active base price already exists for referenceId="
                    + request.getReferenceId() + ", bookingType=" + bookingType);
        }

        BasePrice basePrice = BasePrice.builder().referenceId(request
                .getReferenceId())
                .bookingType(bookingType)
                .price(request.getPrice())
                .currency(request.getCurrency()
                        .trim().toUpperCase())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        BasePrice savedBasePrice = basePriceRepository.save(basePrice);

        log.info("Base price created successfully: basePriceId={}, referenceId={}, bookingType={}",
                savedBasePrice.getId(), savedBasePrice.getReferenceId(), savedBasePrice.getBookingType());
        return toResponse(savedBasePrice);
    }

    @Override
    @Transactional
    public BasePriceResponse updateBasePrice(Long basePriceId, BasePriceRequest request) {

        log.info("Updating base price: basePriceId={}", basePriceId);

        BasePrice basePrice = basePriceRepository.findById(basePriceId)
                .orElseThrow(() -> new BasePriceNotFoundException("Base price not found with id: " + basePriceId));

        basePrice.setReferenceId(request.getReferenceId());
        basePrice.setBookingType(request.getBookingType().trim().toUpperCase());
        basePrice.setPrice(request.getPrice());
        basePrice.setCurrency(request.getCurrency().trim().toUpperCase());
        basePrice.setUpdatedAt(LocalDateTime.now());
        BasePrice updatedBasePrice = basePriceRepository.save(basePrice);
        log.info("Base price updated successfully: basePriceId={}", updatedBasePrice.getId());
        return toResponse(updatedBasePrice);
    }

    @Override
    @Transactional(readOnly = true)
    public BasePriceResponse getBasePriceById(Long basePriceId) {

        log.debug("Fetching base price: basePriceId={}", basePriceId);

        BasePrice basePrice = basePriceRepository.findById(basePriceId)
                .orElseThrow(() -> new BasePriceNotFoundException("Base price not found with id: " + basePriceId));
        return toResponse(basePrice);
    }

    @Override
    @Transactional
    public void deactivateBasePrice(Long basePriceId) {

        log.info("Deactivating base price: basePriceId={}", basePriceId);

        BasePrice basePrice = basePriceRepository.findById(basePriceId)
                .orElseThrow(() -> new BasePriceNotFoundException("Base price not found with id: " + basePriceId));
        basePrice.setActive(false);
        basePrice.setUpdatedAt(LocalDateTime.now());
        basePriceRepository.save(basePrice);
        log.info("Base price deactivated successfully: basePriceId={}", basePriceId);
    }

    private BasePriceResponse toResponse(BasePrice basePrice) {

        return BasePriceResponse.builder().id(basePrice.getId())
                .referenceId(basePrice.getReferenceId())
                .bookingType(basePrice.getBookingType())
                .price(basePrice.getPrice()).currency(basePrice.getCurrency())
                .active(basePrice.getActive())
                .createdAt(basePrice.getCreatedAt())
                .updatedAt(basePrice.getUpdatedAt())
                .build();
    }
}