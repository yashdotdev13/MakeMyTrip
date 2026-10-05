package com.company.MakeMyTrip.pricing_service.dtos;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePriceRequest {

    @NotNull(message = "Reference ID is required")
    @Positive(message = "Reference ID must be greater than 0")
    private Long referenceId;

    @NotBlank(message = "Booking type is required")
    @Size(
            max = 50,
            message = "Booking type must not exceed 50 characters"
    )
    private String bookingType;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.01",
            message = "Price must be greater than 0"
    )
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Size(
            min = 3,
            max = 3,
            message = "Currency must be a 3-letter ISO currency code"
    )
    private String currency;
}