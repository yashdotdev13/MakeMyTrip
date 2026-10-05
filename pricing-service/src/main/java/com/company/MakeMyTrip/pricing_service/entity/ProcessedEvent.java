package com.company.MakeMyTrip.pricing_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "processed_events",
        indexes = {
                @Index(
                        name = "idx_processed_event_event_id",
                        columnList = "event_id",
                        unique = true
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 100
    )
    private String eventId;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}