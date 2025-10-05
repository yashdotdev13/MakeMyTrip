package com.company.MakeMyTrip.pricing_service.dtos;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceRuleRequest {

    private String ruleType;
    private Double factor;
    private String condition;
    private LocalDate startDate;
    private LocalDate endDate;


    private Integer minQuantityThreshold;


    private String inventoryType;
    private String description;
}
