package com.company.MakeMyTrip.booking_service.dtos;

import com.company.MakeMyTrip.booking_service.enums.BookingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class BookingRequest {

    @NotNull(message = "Booking type is required")
    private BookingType bookingType;

    @NotNull(message = "Reference ID is required")
    @Positive(message = "Reference ID must be positive")
    private Long referenceId;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;
}