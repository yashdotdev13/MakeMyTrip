package com.company.MakeMyTrip.agent_service.tool;

import java.util.Map;

public record ToolExecutionResult(
        boolean success,
        Map<String, Object> data,
        String error
) {

    public static ToolExecutionResult success(
            Map<String, Object> data
    ) {
        return new ToolExecutionResult(true, data, null);
    }

    public static ToolExecutionResult failure(String error) {
        return new ToolExecutionResult(false, Map.of(), error);
    }
}