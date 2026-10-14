
package com.company.MakeMyTrip.agent_service.dto;

import java.util.Map;

public record AgentDecision(
        DecisionType type,
        String toolName,
        Map<String, Object> arguments,
        String response
) {
    public enum DecisionType {
        TOOL_CALL,
        TEXT_RESPONSE
    }

    public static AgentDecision toolCall(
            String toolName,
            Map<String, Object> arguments) {
        return new AgentDecision(
                DecisionType.TOOL_CALL,
                toolName,
                arguments,
                null
        );
    }

    public static AgentDecision textResponse(String response) {
        return new AgentDecision(
                DecisionType.TEXT_RESPONSE,
                null,
                Map.of(),
                response
        );
    }
}
