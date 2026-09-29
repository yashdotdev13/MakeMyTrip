package com.company.MakeMyTrip.booking_service.entity;


import com.company.MakeMyTrip.booking_service.enums.BookingStatus;
import com.company.MakeMyTrip.booking_service.enums.BookingType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_booking_user_id", columnList = "user_id"),
                @Index(name = "idx_booking_reference_date",
                        columnList = "reference_id, travel_date"),
                @Index(name = "idx_booking_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    private BookingType bookingType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "booking_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;
}
