
package com.company.MakeMyTrip.agent_service.tool;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ToolOrchestrator {

    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;

    public ToolOrchestrator(
            ToolRegistry toolRegistry,
            ToolExecutor toolExecutor) {
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
    }

    public List<ToolDefinition> getAvailableTools() {
        return List.copyOf(toolRegistry.getToolDefinitions());
    }

    public ToolExecutionResult execute(
            String toolName,
            Map<String, Object> arguments,
            String authorization) {

        return toolExecutor.executeSafely(
                toolName,
                arguments,
                authorization
        );
    }
}
