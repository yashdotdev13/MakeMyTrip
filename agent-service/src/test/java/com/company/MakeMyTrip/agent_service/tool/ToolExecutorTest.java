package com.company.MakeMyTrip.agent_service.tool;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ToolExecutorTest {

    @Test
    void executesRegisteredTool() {
        AgentTool tool = mock(AgentTool.class);

        when(tool.getName()).thenReturn("my_bookings");
        when(tool.execute(anyMap(), anyString()))
                .thenReturn(Map.of("count", 2));

        ToolRegistry registry = new ToolRegistry(
                java.util.List.of(tool)
        );

        ToolExecutor executor = new ToolExecutor(registry);

        Map<String, Object> result = executor.execute(
                "my_bookings",
                Map.of(),
                "Bearer test-token"
        );

        assertEquals(2, result.get("count"));
        verify(tool).execute(Map.of(), "Bearer test-token");
    }

    @Test
    void rejectsUnknownTool() {
        ToolExecutor executor = new ToolExecutor(
                new ToolRegistry(java.util.List.of())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(
                        "unknown_tool",
                        Map.of(),
                        "Bearer test-token"
                )
        );
    }

    @Test
    void rejectsMissingAuthorization() {
        ToolExecutor executor = new ToolExecutor(
                new ToolRegistry(java.util.List.of())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(
                        "my_bookings",
                        Map.of(),
                        " "
                )
        );
    }
}