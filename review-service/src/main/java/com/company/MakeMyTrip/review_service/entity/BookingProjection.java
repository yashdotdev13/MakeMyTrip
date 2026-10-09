package com.company.MakeMyTrip.review_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "booking_projections",
        indexes = @Index(
                name = "idx_booking_projection_user",
                columnList = "user_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingProjection {

    @Id
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String bookingType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant occurredAt;
}