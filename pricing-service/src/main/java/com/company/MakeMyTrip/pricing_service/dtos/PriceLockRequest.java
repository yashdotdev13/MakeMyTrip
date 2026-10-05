package com.company.MakeMyTrip.pricing_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceLockRequest {

    @NotNull(message = "Reference ID is required")
    @Positive(message = "Reference ID must be greater than 0")
    private Long referenceId;

    @NotBlank(message = "Booking type is required")
    @Size(
            max = 50,
            message = "Booking type must not exceed 50 characters"
    )
    private String bookingType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotBlank(message = "Travel date is required")
    @Size(
            min = 10,
            max = 10,
            message = "Travel date must be in yyyy-MM-dd format"
    )
    private String travelDate;
}