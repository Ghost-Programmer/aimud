package io.nadia.ai.aimud.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StatusServiceIntegrationTest {

    @Autowired
    private StatusService statusService;

    @Test
    void getSystemStatus_ReturnsAllFields() {
        Map<String, Object> status = statusService.getSystemStatus().block();

        assertThat(status).isNotNull();
        assertThat(status).containsKeys("status", "database", "version", "uptime", "llmStatus", "llmModel");
        assertThat(status.get("llmStatus")).isIn("Connected", "Disconnected");
        assertThat(status.get("llmModel")).isInstanceOf(String.class);
        assertThat((String) status.get("llmModel")).isNotBlank();
    }
}
