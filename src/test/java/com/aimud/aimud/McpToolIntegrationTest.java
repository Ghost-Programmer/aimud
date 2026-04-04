package com.aimud.aimud;

import com.aimud.aimud.service.McpToolService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class McpToolIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void mcpToolServiceLoads() {
        assertThat(applicationContext.containsBean("mcpToolService")).isTrue();
        McpToolService toolService = applicationContext.getBean(McpToolService.class);
        assertThat(toolService).isNotNull();
    }

    @Test
    void toolMetadataIsPresent() {
        // Just verify the beans are there, specific Tool metadata extraction is more involved 
        // and usually handled by Spring AI internally.
        McpToolService toolService = applicationContext.getBean(McpToolService.class);
        assertThat(toolService.getAllRooms()).isNotNull();
        assertThat(toolService.getAllItems()).isNotNull();
    }
}
