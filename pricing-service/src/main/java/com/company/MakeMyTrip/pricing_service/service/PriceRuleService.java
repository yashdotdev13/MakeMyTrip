package com.company.MakeMyTrip.pricing_service.service;

import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleResponse;

import java.util.List;

public interface PriceRuleService {

    PriceRuleResponse createPriceRule(PriceRuleRequest request);

    PriceRuleResponse updatePriceRule(Long ruleId, PriceRuleRequest request);

    void deletePriceRule(Long ruleId);

    List<PriceRuleResponse> getAllPriceRules();

    PriceRuleResponse getPriceRuleById(Long ruleId);
}
