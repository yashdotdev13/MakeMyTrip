package com.company.MakeMyTrip.pricing_service.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceConfirmRequest {

    @NotNull(message = "Lock ID is required")
    @Positive(message = "Lock ID must be greater than 0")
    private Long lockId;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be greater than 0")
    private Long userId;
}