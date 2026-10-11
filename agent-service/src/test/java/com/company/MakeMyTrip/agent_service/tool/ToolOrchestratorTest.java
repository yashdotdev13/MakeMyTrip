
package com.company.MakeMyTrip.agent_service.tool;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ToolOrchestratorTest {

    @Test
    void getAvailableToolsReturnsRegisteredDefinitions() {
        AgentTool tool = new AgentTool() {
            @Override
            public String getName() {
                return "my_bookings";
            }

            @Override
            public String getDescription() {
                return "Retrieve the authenticated user's bookings.";
            }

            @Override
            public Set<String> getAllowedArguments() {
                return Set.of();
            }

            @Override
            public Map<String, Object> execute(
                    Map<String, Object> arguments,
                    String authorization) {
                return Map.of("count", 0);
            }
        };

        ToolRegistry registry = new ToolRegistry(List.of(tool));
        ToolExecutor executor = new ToolExecutor(registry);
        ToolOrchestrator orchestrator =
                new ToolOrchestrator(registry, executor);

        List<ToolDefinition> definitions =
                orchestrator.getAvailableTools();

        assertEquals(1, definitions.size());
        assertEquals("my_bookings", definitions.get(0).name());
    }

    @Test
    void executeDelegatesToToolExecutor() {
        ToolRegistry registry = mock(ToolRegistry.class);
        ToolExecutor executor = mock(ToolExecutor.class);

        ToolExecutionResult expected =
                ToolExecutionResult.success(Map.of("count", 2));

        when(executor.executeSafely(
                "my_bookings", Map.of(), "Bearer test-token"))
                .thenReturn(expected);

        ToolOrchestrator orchestrator =
                new ToolOrchestrator(registry, executor);

        ToolExecutionResult actual = orchestrator.execute(
                "my_bookings",
                Map.of(),
                "Bearer test-token"
        );

        assertSame(expected, actual);

        verify(executor).executeSafely(
                "my_bookings", Map.of(), "Bearer test-token");
    }
}
