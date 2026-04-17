package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Data object representing in-game NPC or player alignment groups.
 * Impacts store pricing and aggression logic depending on allegiance ratings.
 */
@Getter
@Setter
@Table("factions")
public class Faction {
    @Id
    private Long id;
    
    private String name;
    private String description;
}
