package io.nadia.ai.aimud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Standard data record representing an AI agent's memory or context mapping.
 * Contains generic context content strings associated with titles.
 */
@Table("agents")
public record Agent(
        @Id Long id,
        String title,
        String content) {
}

