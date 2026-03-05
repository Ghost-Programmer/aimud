package com.aimud.aimud.service;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.Map;

@Service
public class StatusService {

    private final DatabaseClient databaseClient;

    public StatusService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Map<String, Object>> getSystemStatus() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptimeInMillis = runtimeBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeInMillis);

        String uptimeString = String.format("%d hours, %d minutes, %d seconds",
                uptime.toHours(),
                uptime.toMinutesPart(),
                uptime.toSecondsPart());

        return databaseClient.sql("SELECT value FROM server_info WHERE key = 'db_status'")
                .map(row -> row.get("value", String.class))
                .one()
                .defaultIfEmpty("Database Disconnected")
                .map(dbMessage -> Map.<String, Object>of(
                        "status", "ONLINE",
                        "database", dbMessage,
                        "version", "0.0.1-SNAPSHOT",
                        "uptime", uptimeString))
                .onErrorResume(e -> Mono.just(Map.<String, Object>of(
                        "status", "ONLINE",
                        "database", "Connection Failed: " + e.getMessage(),
                        "version", "0.0.1-SNAPSHOT",
                        "uptime", uptimeString)));
    }
}
