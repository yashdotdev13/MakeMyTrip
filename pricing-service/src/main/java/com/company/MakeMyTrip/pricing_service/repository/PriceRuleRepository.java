package com.company.MakeMyTrip.pricing_service.repository;

import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRuleRepository extends JpaRepository<PriceRule,Long> {

    // fetch all rules for spccific type( SEASONAL, DEMAND, etc)
    List<PriceRule> findByRuleType(RuleType ruleType);
}
