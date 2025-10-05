package com.company.MakeMyTrip.pricing_service.entity;

import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "price_rules")
public class PriceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    @Column(nullable = false)
    private Double factor;

    @Column(nullable = false)
    private Boolean active = true;


    private LocalDate startDate;
    private LocalDate endDate;


    private Integer minQuantityThreshold;


    private String inventoryType;

    @Column(length = 1000)
    private String description;
}
