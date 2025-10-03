package com.company.MakeMyTrip.pricing_service.service.Impl;


import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleResponse;
import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import com.company.MakeMyTrip.pricing_service.service.PriceRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceRuleServiceImpl implements PriceRuleService {

    private final PriceRuleRepository priceRuleRepository;
    private final ModelMapper modelMapper;
    @Override
    public PriceRuleResponse createPriceRule(PriceRuleRequest request) {
        log.info("Creating price rule: {}", request);

        PriceRule rule = modelMapper.map(request, PriceRule.class);
        PriceRule savedRule = priceRuleRepository.save(rule);

        log.info("Price rule created successfully with id: {}",savedRule.getId());
        return modelMapper.map(savedRule, PriceRuleResponse.class);
    }

    @Override
    public PriceRuleResponse updatePriceRule(Long ruleId, PriceRuleRequest request) {
       log.info("Updating price rule with id: {}",ruleId);

       PriceRule rule = priceRuleRepository.findById(ruleId)
               .orElseThrow(()->new RuntimeException("Price rule not found with id: "+ruleId));

        rule.setRuleType(RuleType.valueOf(request.getRuleType()));
        rule.setFactor(request.getFactor());
        rule.setCondition(request.getCondition());

        PriceRule updatedRule = priceRuleRepository.save(rule);

        log.info("Price rule updated successfully: {}",ruleId);
        return modelMapper.map(updatedRule, PriceRuleResponse.class);
    }

    @Override
    public void deletePriceRule(Long ruleId) {

    }

    @Override
    public List<PriceRuleResponse> getAllPriceRules() {
        return List.of();
    }

    @Override
    public PriceRuleResponse getPriceRuleById(Long ruleId) {
        return null;
    }
}
