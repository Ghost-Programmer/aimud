package io.nadia.ai.aimud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("agents")
public record Agent(
        @Id Long id,
        String title,
        String content) {
}

