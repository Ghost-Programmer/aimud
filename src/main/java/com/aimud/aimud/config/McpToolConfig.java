package com.aimud.aimud.config;

import com.aimud.aimud.service.McpToolService;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpToolConfig {

    @Bean
    public List<FunctionCallback> mcpTools(McpToolService mcpToolService) {
        return List.of((FunctionCallback[]) ToolCallbacks.from(mcpToolService));
    }
}
