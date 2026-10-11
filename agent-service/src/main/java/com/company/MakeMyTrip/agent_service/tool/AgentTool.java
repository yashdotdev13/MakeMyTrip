package com.company.MakeMyTrip.agent_service.tool;

import java.util.Map;
import java.util.Set;

public interface AgentTool {

    String getName();

    String getDescription();

    Set<String> getAllowedArguments();

    Map<String, Object> execute(
            Map<String, Object> arguments,
            String authorization
    );
}