package com.company.MakeMyTrip.booking_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingConfirmationRequest {

    @NotNull(message = "Booking ID is required")
    @Positive(message = "Booking ID must be positive")
    private Long bookingId;

    @NotNull(message = "Quoted price is required")
    @Positive(message = "Quoted price must be positive")
    private BigDecimal quotedPrice;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}