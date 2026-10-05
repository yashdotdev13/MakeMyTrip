package com.company.MakeMyTrip.pricing_service.entity;

import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "price_rules",
        indexes = {
                @Index(
                        name = "idx_price_rule_active",
                        columnList = "active"
                ),
                @Index(
                        name = "idx_price_rule_type_active",
                        columnList = "rule_type, active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RuleType ruleType;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal factor;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer minQuantityThreshold;

    @Column(length = 50)
    private String inventoryType;

    @Column(length = 1000)
    private String description;
}