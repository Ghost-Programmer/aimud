package com.aimud.aimud;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatusController {

        private final DatabaseClient databaseClient;

        public StatusController(DatabaseClient databaseClient) {
                this.databaseClient = databaseClient;
        }

        @GetMapping("/status")
        public Mono<Map<String, Object>> getStatus() {
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
