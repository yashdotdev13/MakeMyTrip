package com.company.MakeMyTrip.pricing_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "demand_projections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_demand_projection_reference_booking_date",
                        columnNames = {
                                "reference_id",
                                "booking_type",
                                "travel_date"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_demand_projection_reference",
                        columnList = "reference_id"
                ),
                @Index(
                        name = "idx_demand_projection_travel_date",
                        columnList = "travel_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false, length = 50)
    private String bookingType;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentBookings = 0;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}