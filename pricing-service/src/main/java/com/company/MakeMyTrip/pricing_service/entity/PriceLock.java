package com.company.MakeMyTrip.pricing_service.entity;

import com.company.MakeMyTrip.pricing_service.enums.PriceLockStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "price_locks",
        indexes = {
                @Index(
                        name = "idx_price_lock_reference_booking_user",
                        columnList = "reference_id, booking_type, user_id"
                ),
                @Index(
                        name = "idx_price_lock_valid_till",
                        columnList = "valid_till"
                ),
                @Index(
                        name = "idx_price_lock_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false, length = 50)
    private String bookingType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal adjustedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceLockStatus status;

    @Column(nullable = false)
    private LocalDateTime validTill;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 3)
    private String currency;
}