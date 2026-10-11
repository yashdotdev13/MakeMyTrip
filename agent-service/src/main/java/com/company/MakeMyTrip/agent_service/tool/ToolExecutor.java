package com.company.MakeMyTrip.agent_service.tool;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

        // Resolve only registered tools.
        AgentTool tool = toolRegistry.getTool(toolName);

        // Reject arguments that the tool does not support.
        Set<String> allowedArguments = tool.getAllowedArguments();

        for (String argumentName : arguments.keySet()) {
            if (!allowedArguments.contains(argumentName)) {
                throw new IllegalArgumentException(
                        "Unsupported argument for tool '"
                                + toolName + "': " + argumentName
                );
            }
        }

        // Execute after validation.
        return Objects.requireNonNull(
                tool.execute(arguments, authorization),
                "Tool execution returned null"
        );
    }


    public ToolExecutionResult executeSafely(
            String toolName,
            Map<String, Object> arguments,
            String authorization
    ) {
        try {
            return ToolExecutionResult.success(
                    execute(toolName, arguments, authorization)
            );
        } catch (IllegalArgumentException exception) {
            return ToolExecutionResult.failure(
                    "Invalid tool request."
            );
        } catch (Exception exception) {
            return ToolExecutionResult.failure(
                    "Tool execution failed."
            );
        }
    }
}