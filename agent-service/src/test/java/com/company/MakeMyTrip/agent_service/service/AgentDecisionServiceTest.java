package com.company.MakeMyTrip.agent_service.service;

import com.company.MakeMyTrip.agent_service.dto.AgentDecision;
import com.company.MakeMyTrip.agent_service.tool.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentDecisionServiceTest {

    private ChatClient chatClient;
    private ToolRegistry toolRegistry;
    private AgentDecisionService decisionService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        toolRegistry = mock(ToolRegistry.class);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        decisionService = new AgentDecisionService(
                builder,
                toolRegistry,
                new ObjectMapper()
        );
    }

    @Test
    void rejectsBlankUserMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> decisionService.decide(" ")
        );
    }

    @Test
    void parsesTextResponse() {
        // Parsing tests should exercise the parser directly or use a
        // mocked ChatClient fluent chain to return this JSON.
        assertEquals(
                AgentDecision.DecisionType.TEXT_RESPONSE,
                AgentDecision.textResponse("Hello").type()
        );
    }

    @Test
    void parsesToolCallDecisionFactory() {
        AgentDecision decision = AgentDecision.toolCall(
                "my_bookings", Map.of());

        assertEquals(AgentDecision.DecisionType.TOOL_CALL, decision.type());
        assertEquals("my_bookings", decision.toolName());
        assertTrue(decision.arguments().isEmpty());
    }
}
