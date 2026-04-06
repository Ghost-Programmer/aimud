package com.aimud.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("factions")
public class Faction {
    @Id
    private Long id;
    
    private String name;
    private String description;
}
