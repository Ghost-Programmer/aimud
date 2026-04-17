package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Associative tracking linking a specific Mobile to an overarching {@link Faction}.
 * Governs alignment scaling, vendor prices, and aggro response.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("mobile_factions")
public class MobileFaction {
    
    @Column("mobile_id")
    private Long mobileId;
    
    @Column("faction_id")
    private Long factionId;
    
    private int rating;
}
