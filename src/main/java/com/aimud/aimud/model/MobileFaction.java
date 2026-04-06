package com.aimud.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

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
