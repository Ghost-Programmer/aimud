package com.aimud.aimud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StatusService {

    private final DatabaseClient databaseClient;
    private final OllamaChatModel ollamaChatModel;

    @Value("${spring.ai.ollama.chat.options.model:qwen3.5}")
    private String modelName;

    public StatusService(DatabaseClient databaseClient, OllamaChatModel ollamaChatModel) {
        this.databaseClient = databaseClient;
        this.ollamaChatModel = ollamaChatModel;
    }

    public Mono<Map<String, Object>> getSystemStatus() {
        log.info("Retrieving system status");
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptimeInMillis = runtimeBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeInMillis);

        String uptimeString = String.format("%d hours, %d minutes, %d seconds",
                uptime.toHours(),
                uptime.toMinutesPart(),
                uptime.toSecondsPart());

        // Simple check for LLM connection (just confirming the bean is present and modelName is set)
        String llmStatus = ollamaChatModel != null ? "Connected" : "Disconnected";
        log.debug("LLM Status: {}, Model: {}", llmStatus, modelName);

        return databaseClient.sql("SELECT value FROM server_info WHERE key = 'db_status'")
                .map(row -> row.get("value", String.class))
                .one()
                .defaultIfEmpty("Database Disconnected")
                .map(dbMessage -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("status", "ONLINE");
                    status.put("database", dbMessage);
                    status.put("version", "0.0.1-SNAPSHOT");
                    status.put("uptime", uptimeString);
                    status.put("llmStatus", llmStatus);
                    status.put("llmModel", modelName);
                    return status;
                })
                .onErrorResume(e -> {
                    log.error("Failed to retrieve database status", e);
                    Map<String, Object> status = new HashMap<>();
                    status.put("status", "ONLINE");
                    status.put("database", "Connection Failed: " + e.getMessage());
                    status.put("version", "0.0.1-SNAPSHOT");
                    status.put("uptime", uptimeString);
                    status.put("llmStatus", llmStatus);
                    status.put("llmModel", modelName);
                    return Mono.just(status);
                });
    }
}
