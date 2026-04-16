package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("mobile_macros")
public class MobileMacro {
    @Id
    private Long id;

    @Column("mobile_id")
    private Long mobileId;

    @Column("macro_index")
    private int macroIndex;

    private String label;
    private String command;
}
