package io.nadia.ai.aimud.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Canonical dictionary lookup entity storing the fully qualified names of all valid abilities.
 * Used for hard validation during skill assignment logic.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("skills_registry")
public class SkillRegistry {
    @Id
    private Long id;

    private String name;
}
