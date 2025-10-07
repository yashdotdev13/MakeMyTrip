package com.company.MakeMyTrip.payment_service.entity;


import com.company.MakeMyTrip.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long bookingId;
    private Long userId;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String paymentMethod;    // STRIPE, PAYPAL
    private String transactionId;

    private LocalDateTime paymentTime;
}
