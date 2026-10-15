package com.company.MakeMyTrip.agent_service.service;

import com.company.MakeMyTrip.agent_service.dto.AgentDecision;
import com.company.MakeMyTrip.agent_service.dto.AgentRequestResult;
import com.company.MakeMyTrip.agent_service.tool.ToolExecutionResult;
import com.company.MakeMyTrip.agent_service.tool.ToolOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentRequestOrchestratorTest {

    @Mock
    private AgentDecisionService decisionService;

    @Mock
    private ToolOrchestrator toolOrchestrator;

    private AgentRequestOrchestrator requestOrchestrator;

    @BeforeEach
    void setUp() {
        requestOrchestrator = new AgentRequestOrchestrator(
                decisionService,
                toolOrchestrator
        );
    }

    @Test
    void shouldReturnTextResponseWithoutExecutingTool() {
        String userMessage = "Hello";
        String response = "How can I help with your trip?";

        when(decisionService.decide(userMessage))
                .thenReturn(AgentDecision.textResponse(response));

        AgentRequestResult result =
                requestOrchestrator.handle(userMessage, "Bearer token");

        assertEquals(
                AgentDecision.DecisionType.TEXT_RESPONSE,
                result.type()
        );
        assertEquals(response, result.response());
        assertNull(result.toolResult());

        verify(toolOrchestrator, never())
                .execute(anyString(), anyMap(), anyString());
    }

    @Test
    void shouldExecuteToolForToolCallDecision() {
        String userMessage = "Show me my bookings";
        String authorization = "Bearer test-token";
        Map<String, Object> arguments = Map.of();

        AgentDecision decision =
                AgentDecision.toolCall("my_bookings", arguments);

        ToolExecutionResult executionResult =
                ToolExecutionResult.success(
                        Map.of("count", 2)
                );

        when(decisionService.decide(userMessage))
                .thenReturn(decision);

        when(toolOrchestrator.execute(
                "my_bookings",
                arguments,
                authorization
        )).thenReturn(executionResult);

        AgentRequestResult result =
                requestOrchestrator.handle(userMessage, authorization);

        assertEquals(
                AgentDecision.DecisionType.TOOL_CALL,
                result.type()
        );
        assertSame(executionResult, result.toolResult());
        assertNull(result.response());

        verify(toolOrchestrator).execute(
                "my_bookings",
                arguments,
                authorization
        );
    }

    @Test
    void shouldPreserveToolExecutionFailure() {
        String userMessage = "Show me my bookings";
        String authorization = "Bearer test-token";

        ToolExecutionResult failure =
                ToolExecutionResult.failure("Tool execution failed.");

        when(decisionService.decide(userMessage))
                .thenReturn(
                        AgentDecision.toolCall("my_bookings", Map.of())
                );

        when(toolOrchestrator.execute(
                "my_bookings",
                Map.of(),
                authorization
        )).thenReturn(failure);

        AgentRequestResult result =
                requestOrchestrator.handle(userMessage, authorization);

        assertNotNull(result.toolResult());
        assertFalse(result.toolResult().success());
        assertEquals(
                "Tool execution failed.",
                result.toolResult().error()
        );
    }

    @Test
    void shouldRejectBlankUserMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> requestOrchestrator.handle("  ", "Bearer token")
        );

        verifyNoInteractions(decisionService, toolOrchestrator);
    }

    @Test
    void shouldRejectNullDecision() {
        when(decisionService.decide("Show my bookings"))
                .thenReturn(null);

        assertThrows(
                NullPointerException.class,
                () -> requestOrchestrator.handle(
                        "Show my bookings",
                        "Bearer token"
                )
        );

        verifyNoInteractions(toolOrchestrator);
    }
}
