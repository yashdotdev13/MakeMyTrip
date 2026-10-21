package com.company.MakeMyTrip.agent_service.service;

import com.company.MakeMyTrip.agent_service.dto.AgentDecision;
import com.company.MakeMyTrip.agent_service.dto.AgentRequestResult;
import com.company.MakeMyTrip.agent_service.tool.ToolOrchestrator;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AgentRequestOrchestrator {

    private final AgentDecisionService decisionService;
    private final ToolOrchestrator toolOrchestrator;

    public AgentRequestOrchestrator(AgentDecisionService decisionService, ToolOrchestrator toolOrchestrator) {
        this.decisionService = decisionService;
        this.toolOrchestrator = toolOrchestrator;
    }

    public AgentRequestResult handle(String userMessage, String authorization) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("User message must not be blank");
        }

        AgentDecision decision = Objects.requireNonNull(decisionService.decide(userMessage),
                "Decision service returned null");

        if (decision.type() == AgentDecision.DecisionType.TEXT_RESPONSE) {
            return AgentRequestResult.textResponse(decision.response());
        }

        if (decision.type() == AgentDecision.DecisionType.TOOL_CALL) {
            var result = toolOrchestrator.execute(decision.toolName(), decision.arguments(), authorization);
            return AgentRequestResult.toolResult(result);
        }
        throw new IllegalArgumentException("Unsupported agent decision type");
    }
}
