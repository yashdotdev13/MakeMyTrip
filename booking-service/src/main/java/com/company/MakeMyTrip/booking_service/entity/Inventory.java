package com.company.MakeMyTrip.booking_service.entity;

import com.company.MakeMyTrip.booking_service.enums.BookingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_booking_reference_date",
                        columnNames = {"booking_type", "reference_id", "travel_date"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_inventory_reference_date",
                        columnList = "reference_id, travel_date"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    private BookingType bookingType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "confirmed_quantity", nullable = false)
    @Builder.Default
    private Integer confirmedQuantity = 0;

    @Version
    private Long version;

    public int getAvailableCapacity() {
        return totalCapacity - reservedQuantity - confirmedQuantity;
    }
}