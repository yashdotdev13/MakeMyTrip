package com.company.MakeMyTrip.booking_service.dtos;

import com.company.MakeMyTrip.booking_service.enums.BookingStatus;
import com.company.MakeMyTrip.booking_service.enums.BookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long userId;

    private BookingType bookingType;
    private Long referenceId;

    private BookingStatus status;

    private LocalDateTime bookingDate;
    private LocalDate travelDate;

    private BigDecimal amount;
}