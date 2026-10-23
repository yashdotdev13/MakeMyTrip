package com.company.MakeMyTrip.agent_service.service;

import com.company.MakeMyTrip.agent_service.dto.AgentDecision;
import com.company.MakeMyTrip.agent_service.tool.ToolDefinition;
import com.company.MakeMyTrip.agent_service.tool.ToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AgentDecisionService {

    private final ChatClient chatClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public AgentDecisionService(ChatClient.Builder chatClientBuilder, ToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }
    public AgentDecision decide(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("User message must not be blank");
        }
        String toolDefinitions = toolRegistry.getToolDefinitions().stream().map(this::formatToolDefinition)
                .collect(Collectors.joining("\n"));
        String prompt = """
                You are the decision engine for a travel booking platform.
                
                Select a tool only when the user's request requires one.
                Select tools exclusively from the provided list.
                Never invent tool names or arguments.
                Do not execute tools or claim that an action has been completed.
                
                Available tools:
                %s
                
                Return exactly one JSON object in one of these formats:
                
                For a tool call:
                {"type":"TOOL_CALL","toolName":"my_bookings","arguments":{}}
                
                For a text response:
                {"type":"TEXT_RESPONSE","response":"Your response"}
                
                User request:
                %s
                """.formatted(toolDefinitions, userMessage);

        String modelResponse = chatClient.prompt().user(prompt).call().content();
        return parseAndValidate(modelResponse);
    }

    private String formatToolDefinition(ToolDefinition definition) {
        return """
                Name: %s
                Description: %s
                Allowed arguments: %s
                """.formatted(definition.name(), definition.description(), definition.allowedArguments());
    }

    private AgentDecision parseAndValidate(String modelResponse) {
        if (modelResponse == null || modelResponse.isBlank()) {
            throw new IllegalStateException("The decision model returned an empty response");
        }
        try {
            Map<?, ?> payload = objectMapper.readValue(modelResponse, Map.class);
            Object rawType = payload.get("type");
            if (!(rawType instanceof String type)) {
                throw new IllegalArgumentException("Decision type is missing");
            }
            if ("TEXT_RESPONSE".equals(type)) {
                Object rawResponse = payload.get("response");
                if (!(rawResponse instanceof String response) || response.isBlank()) {
                    throw new IllegalArgumentException("Text response must not be blank");
                }
                return AgentDecision.textResponse(response);
            }
            if ("TOOL_CALL".equals(type)) {
                return parseToolCall(payload);
            }
            throw new IllegalArgumentException("Unsupported decision type");
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("The decision model returned invalid JSON", exception);
        }
    }

    private AgentDecision parseToolCall(Map<?, ?> payload) {
        Object rawName = payload.get("toolName");
        Object rawArguments = payload.get("arguments");
        if (!(rawName instanceof String toolName) || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name must not be blank");
        }
        if (!(rawArguments instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Tool arguments must be a JSON object");
        }
        Map<String, Object> arguments = rawMap.entrySet().stream().collect(Collectors.toMap(entry -> {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("Argument names must be strings");
            }
            return key;
        }, Map.Entry::getValue));
        ToolDefinition definition = toolRegistry.getToolDefinitions().stream().filter(tool -> tool.name()
                        .equals(toolName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The model selected an unknown tool"));
        Set<String> allowedArguments = definition.allowedArguments();
        if (!allowedArguments.containsAll(arguments.keySet())) {
            throw new IllegalArgumentException("The model supplied unsupported tool arguments");
        }
        return AgentDecision.toolCall(toolName, arguments);
    }
}
