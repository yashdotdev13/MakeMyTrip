package com.company.MakeMyTrip.agent_service.tool;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ToolRegistryTest {

    @Test
    void exposesRegisteredToolDefinitions() {
        AgentTool tool = mock(AgentTool.class);

        when(tool.getName()).thenReturn("my_bookings");
        when(tool.getDescription())
                .thenReturn("Retrieve the authenticated user's bookings.");
        when(tool.getAllowedArguments()).thenReturn(Set.of());

        ToolRegistry registry = new ToolRegistry(List.of(tool));

        List<ToolDefinition> definitions = registry.getToolDefinitions();

        assertEquals(1, definitions.size());
        assertEquals("my_bookings", definitions.get(0).name());
        assertEquals(Set.of(), definitions.get(0).allowedArguments());
    }
}