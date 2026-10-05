package com.company.MakeMyTrip.pricing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePriceResponse {

    private Long id;

    private Long referenceId;

    private String bookingType;

    private BigDecimal price;

    private String currency;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}