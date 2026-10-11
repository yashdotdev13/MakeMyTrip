package com.company.MakeMyTrip.agent_service.tool;

import java.util.Map;

public interface  AgentTool {

    String getName();

    String getDescription();

    Map<String, Object> execute(Map<String, Object> arguments,
                                String authorization);
}
