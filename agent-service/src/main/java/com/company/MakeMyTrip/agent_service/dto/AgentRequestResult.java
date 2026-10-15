package com.company.MakeMyTrip.agent_service.dto;

import com.company.MakeMyTrip.agent_service.tool.ToolExecutionResult;

public record AgentRequestResult(
        AgentDecision.DecisionType type,
        String response,
        ToolExecutionResult toolResult
) {

    public static AgentRequestResult textResponse(String response) {
        return new AgentRequestResult(
                AgentDecision.DecisionType.TEXT_RESPONSE,
                response,
                null
        );
    }

    public static AgentRequestResult toolResult(
            ToolExecutionResult result
    ) {
        return new AgentRequestResult(
                AgentDecision.DecisionType.TOOL_CALL,
                null,
                result
        );
    }
}