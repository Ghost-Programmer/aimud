package io.nadia.ai.aimud.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Link entity tracking an active character's known skill or spell ranks.
 * Associated specifically with player characters or mobs during instancing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("skills")
public class Skill {
    @Id
    private Long id;

    private String name;

    private int rank;

    @Column("character_id")
    private Long characterId;
}
