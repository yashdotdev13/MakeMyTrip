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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceRuleServiceImpl implements PriceRuleService {

    private final PriceRuleRepository priceRuleRepository;
    private final ModelMapper modelMapper;

    @Override
    public PriceRuleResponse createPriceRule(PriceRuleRequest request) {
        log.info("Creating price rule: {}", request);

        PriceRule rule = PriceRule.builder()
                .ruleType(RuleType.valueOf(request.getRuleType()))
                .factor(request.getFactor()) // factor used directly for dynamic calculation
                .active(true)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .minQuantityThreshold(request.getMinQuantityThreshold())
                .inventoryType(request.getInventoryType())
                .description(request.getDescription())
                .build();

        PriceRule savedRule = priceRuleRepository.save(rule);
        log.info("Price rule created successfully with id: {}", savedRule.getId());

        return modelMapper.map(savedRule, PriceRuleResponse.class);
    }

    @Override
    public PriceRuleResponse updatePriceRule(Long ruleId, PriceRuleRequest request) {
        log.info("Updating price rule with id: {}", ruleId);

        PriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Price rule not found with id: " + ruleId));

        rule.setRuleType(RuleType.valueOf(request.getRuleType()));
        rule.setFactor(request.getFactor());
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setMinQuantityThreshold(request.getMinQuantityThreshold());
        rule.setInventoryType(request.getInventoryType());
        rule.setDescription(request.getDescription());
        rule.setActive(true);

        PriceRule updatedRule = priceRuleRepository.save(rule);
        log.info("Price rule updated successfully: {}", ruleId);

        return modelMapper.map(updatedRule, PriceRuleResponse.class);
    }

    @Override
    public void deletePriceRule(Long ruleId) {
        log.info("Deleting price rule with id: {}", ruleId);
        PriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Price rule not found with id: " + ruleId));
        priceRuleRepository.delete(rule);
        log.info("Price rule deleted successfully: {}", ruleId);
    }

    @Override
    public List<PriceRuleResponse> getAllPriceRules() {
        List<PriceRule> rules = priceRuleRepository.findAll();
        return rules.stream()
                .map(rule -> modelMapper.map(rule, PriceRuleResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public PriceRuleResponse getPriceRuleById(Long ruleId) {
        PriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Price rule not found with id: " + ruleId));
        return modelMapper.map(rule, PriceRuleResponse.class);
    }
}
