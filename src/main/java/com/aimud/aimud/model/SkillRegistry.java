package com.aimud.aimud.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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
