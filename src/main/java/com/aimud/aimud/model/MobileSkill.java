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
@Table("mobile_skills")
public class MobileSkill {

    @Id
    private Long id;

    @Column("mobile_id")
    private Long mobileId;

    private String name;

    private int rank;
}

