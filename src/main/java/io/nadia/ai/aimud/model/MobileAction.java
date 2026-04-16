package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("mobile_actions")
public class MobileAction {
    @Id
    private Long id;

    @Column("mobile_id")
    private Long mobileId;

    @Column("action_command")
    private String actionCommand;

    private String description;
}
