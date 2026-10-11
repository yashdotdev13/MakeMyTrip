package com.company.MakeMyTrip.agent_service.tool;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools;

    public ToolRegistry(Collection<AgentTool> agentTools) {
        this.tools = agentTools.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AgentTool::getName,
                        Function.identity()
                ));
    }

    public AgentTool getTool(String name) {
        AgentTool tool = tools.get(name);

        if (tool == null) {
            throw new IllegalArgumentException(
                    "Unknown agent tool: " + name
            );
        }

        return tool;
    }

    public Collection<AgentTool> getAllTools() {
        return tools.values();
    }


    public java.util.List<ToolDefinition> getToolDefinitions() {
        return tools.values().stream()
                .map(tool -> new ToolDefinition(
                        tool.getName(),
                        tool.getDescription(),
                        tool.getAllowedArguments()
                ))
                .toList();
    }
}