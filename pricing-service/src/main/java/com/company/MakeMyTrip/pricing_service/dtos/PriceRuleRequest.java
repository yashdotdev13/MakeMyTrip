package com.company.MakeMyTrip.pricing_service.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceRuleRequest {

    @NotBlank(message = "Rule type is required")
    @Size(max = 50, message = "Rule type must not exceed 50 characters")
    private String ruleType;

    @NotNull(message = "Factor is required")
    @DecimalMin(value = "-100.0000", message = "Factor must be greater than or equal to -100")
    @DecimalMax(value = "100.0000", message = "Factor must be less than or equal to 100")
    private BigDecimal factor;

    @Size(max = 500, message = "Condition must not exceed 500 characters")
    private String condition;

    private LocalDate startDate;

    private LocalDate endDate;

    @Positive(message = "Minimum quantity threshold must be greater than 0")
    private Integer minQuantityThreshold;

    @Size(max = 50, message = "Inventory type must not exceed 50 characters")
    private String inventoryType;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}