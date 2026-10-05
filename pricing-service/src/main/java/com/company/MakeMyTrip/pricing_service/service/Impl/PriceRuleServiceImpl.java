package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleResponse;
import com.company.MakeMyTrip.pricing_service.entity.PriceRule;
import com.company.MakeMyTrip.pricing_service.enums.RuleType;
import com.company.MakeMyTrip.pricing_service.repository.PriceRuleRepository;
import com.company.MakeMyTrip.pricing_service.service.PriceRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceRuleServiceImpl implements PriceRuleService {

    private final PriceRuleRepository priceRuleRepository;

    @Override
    public PriceRuleResponse createPriceRule(PriceRuleRequest request) {

        log.info(
                "Creating price rule: ruleType={}, factor={}",
                request.getRuleType(),
                request.getFactor()
        );

        RuleType ruleType = parseRuleType(request.getRuleType());

        PriceRule priceRule = PriceRule.builder()
                .ruleType(ruleType)
                .factor(request.getFactor())
                .active(true)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .minQuantityThreshold(request.getMinQuantityThreshold())
                .inventoryType(request.getInventoryType())
                .description(request.getDescription())
                .build();

        PriceRule savedRule = priceRuleRepository.save(priceRule);

        log.info(
                "Price rule created successfully: ruleId={}, ruleType={}",
                savedRule.getId(),
                savedRule.getRuleType()
        );

        return toResponse(
                savedRule,
                request.getCondition()
        );
    }

    @Override
    public PriceRuleResponse updatePriceRule(
            Long ruleId,
            PriceRuleRequest request
    ) {

        log.info("Updating price rule: ruleId={}", ruleId);

        PriceRule priceRule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Price rule not found with id: " + ruleId
                        )
                );

        RuleType ruleType = parseRuleType(request.getRuleType());

        priceRule.setRuleType(ruleType);
        priceRule.setFactor(request.getFactor());
        priceRule.setStartDate(request.getStartDate());
        priceRule.setEndDate(request.getEndDate());
        priceRule.setMinQuantityThreshold(
                request.getMinQuantityThreshold()
        );
        priceRule.setInventoryType(request.getInventoryType());
        priceRule.setDescription(request.getDescription());
        priceRule.setActive(true);

        PriceRule updatedRule = priceRuleRepository.save(priceRule);

        log.info(
                "Price rule updated successfully: ruleId={}",
                updatedRule.getId()
        );

        return toResponse(
                updatedRule,
                request.getCondition()
        );
    }

    @Override
    public void deletePriceRule(Long ruleId) {

        log.info("Deleting price rule: ruleId={}", ruleId);

        PriceRule priceRule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Price rule not found with id: " + ruleId
                        )
                );

        priceRuleRepository.delete(priceRule);

        log.info(
                "Price rule deleted successfully: ruleId={}",
                ruleId
        );
    }

    @Override
    public List<PriceRuleResponse> getAllPriceRules() {

        log.info("Fetching all pricing rules");

        return priceRuleRepository.findAll()
                .stream()
                .map(priceRule -> toResponse(priceRule, null))
                .toList();
    }

    @Override
    public PriceRuleResponse getPriceRuleById(Long ruleId) {

        log.info(
                "Fetching price rule: ruleId={}",
                ruleId
        );

        PriceRule priceRule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Price rule not found with id: " + ruleId
                        )
                );

        return toResponse(priceRule, null);
    }

    private PriceRuleResponse toResponse(
            PriceRule priceRule,
            String condition
    ) {

        return PriceRuleResponse.builder()
                .id(priceRule.getId())
                .ruleType(priceRule.getRuleType().name())
                .factor(priceRule.getFactor())
                .condition(condition)
                .startDate(priceRule.getStartDate())
                .endDate(priceRule.getEndDate())
                .minQuantityThreshold(
                        priceRule.getMinQuantityThreshold()
                )
                .inventoryType(priceRule.getInventoryType())
                .description(priceRule.getDescription())
                .build();
    }

    private RuleType parseRuleType(String ruleType) {

        try {
            return RuleType.valueOf(
                    ruleType.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid pricing rule type: " + ruleType
            );
        }
    }
}