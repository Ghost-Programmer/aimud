package com.aimud.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

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
