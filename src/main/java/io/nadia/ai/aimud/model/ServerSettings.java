package io.nadia.ai.aimud.model;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * High-order application configuration keys defining global uptime behavior.
 * Evaluated periodically to control things like maintenance modes.
 */
@Table("server_settings")
public record ServerSettings(
        @Id Long id,
        @Column("server_name") String serverName,
        @Column("allow_new_user") boolean allowNewUser,
        @Column("maintenance") boolean maintenance,
        @Column("maintenance_text") String maintenanceText,
        @Column("mud_hour") int mudHour,
        @Column("mud_day") int mudDay,
        @Column("mud_month") int mudMonth,
        @Column("mud_year") int mudYear,
        @CreatedDate @Column("created_at") LocalDateTime createdAt,
        @LastModifiedDate @Column("modified_at") LocalDateTime modifiedAt,
        @CreatedBy @Column("created_by") String createdBy,
        @LastModifiedBy @Column("modified_by") String modifiedBy) {
}
