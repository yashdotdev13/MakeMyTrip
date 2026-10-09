package com.company.MakeMyTrip.review_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "booking_projections",
        indexes = {
                @Index(
                        name = "idx_booking_projection_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingProjection {

    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booking_type", nullable = false, length = 30)
    private String bookingType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}