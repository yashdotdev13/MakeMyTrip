package com.company.MakeMyTrip.pricing_service.controller;


import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleRequest;
import com.company.MakeMyTrip.pricing_service.dtos.PriceRuleResponse;
import com.company.MakeMyTrip.pricing_service.service.PriceRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing/rules")
@RequiredArgsConstructor
@Slf4j
public class PriceRuleController {

    private final PriceRuleService priceRuleService;

    @PostMapping
    public ResponseEntity<PriceRuleResponse> createPriceRule(@RequestBody PriceRuleRequest request){
        log.info("Received request for create the price rule: {}",request);
        PriceRuleResponse response = priceRuleService.createPriceRule(request);
        return ResponseEntity.ok(response);
    }

    // update the existing the price rule
    @PutMapping("/{ruleId}")
    public ResponseEntity<PriceRuleResponse> updatePriceRule(
            @PathVariable Long ruleId,
            @RequestBody PriceRuleRequest request){
        log.info("Received request to update price rule id={} with data: {}",ruleId, request);
        PriceRuleResponse response = priceRuleService.updatePriceRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    // delete the price rule
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deletePriceRule(@PathVariable Long ruleId){
        log.info("Received request to delete price rule id={}", ruleId);
        priceRuleService.deletePriceRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    // Get a price rule by ID
    @GetMapping("/{ruleId}")
    public ResponseEntity<PriceRuleResponse> getPriceRuleById(@PathVariable Long ruleId) {
        log.info("Received request to fetch price rule id={}", ruleId);
        PriceRuleResponse response = priceRuleService.getPriceRuleById(ruleId);
        return ResponseEntity.ok(response);
    }

    // Get all price rules
    @GetMapping
    public ResponseEntity<List<PriceRuleResponse>> getAllPriceRules() {
        log.info("Received request to fetch all price rules");
        List<PriceRuleResponse> rules = priceRuleService.getAllPriceRules();
        return ResponseEntity.ok(rules);
    }
}
