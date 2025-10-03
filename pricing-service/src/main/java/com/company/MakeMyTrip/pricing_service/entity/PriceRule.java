package com.company.MakeMyTrip.pricing_service.entity;

import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(length = 1000)
    private String condition;


    @Column(nullable = false)
    private Boolean active = true;

}
