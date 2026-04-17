package io.nadia.ai.aimud.config;

import io.nadia.ai.aimud.service.McpToolService;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Application configuration wrapper for McpToolConfig.
 */
@Configuration
public class McpToolConfig {

    /**
     * Configures the component for mcp tools.
     * @return constructed List<FunctionCallback> dependency
     */
    @Bean
    public List<FunctionCallback> mcpTools(McpToolService mcpToolService) {
        return List.of(ToolCallbacks.from(mcpToolService));
    }
}
