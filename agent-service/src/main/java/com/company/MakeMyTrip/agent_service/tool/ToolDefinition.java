package com.company.MakeMyTrip.agent_service.tool;

import java.util.Set;

public record ToolDefinition(
        String name,
        String description,
        Set<String> allowedArguments
) {
}