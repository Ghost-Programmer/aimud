package com.aimud.aimud;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("server_settings")
public record ServerSettings(
        @Id Long id,
        @Column("server_name") String serverName,
        @Column("allow_new_user") boolean allowNewUser,
        @Column("maintenance") boolean maintenance,
        @Column("maintenance_text") String maintenanceText) {
}
