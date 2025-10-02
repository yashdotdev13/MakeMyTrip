package com.company.MakeMyTrip.pricing_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_locks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceLock {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long referenceId; // flightId, hotelId,,etc

    private String bookingType;

    private Long userId;

    private Double basePrice;
    private Double finalPrice;

    private boolean locked;

    private LocalDateTime validTill;  // until when price is guaranteed
}
