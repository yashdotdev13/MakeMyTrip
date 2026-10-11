package com.company.MakeMyTrip.agent_service.tool;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class ToolExecutor {

    private final ToolRegistry toolRegistry;

    public ToolExecutor(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public Map<String, Object> execute(
            String toolName,
            Map<String, Object> arguments,
            String authorization
    ) {
        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException(
                    "Tool name must not be blank"
            );
        }

        Objects.requireNonNull(
                arguments,
                "Tool arguments must not be null"
        );

        if (authorization == null || authorization.isBlank()) {
            throw new IllegalArgumentException(
                    "Authorization must not be blank"
            );
        }

        AgentTool tool = toolRegistry.getTool(toolName);

        return Objects.requireNonNull(
                tool.execute(arguments, authorization),
                "Tool execution returned null"
        );
    }
}