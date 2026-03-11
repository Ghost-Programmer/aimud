package com.aimud.aimud.model;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("server_settings")
public record ServerSettings(
        @Id Long id,
        @Column("server_name") String serverName,
        @Column("allow_new_user") boolean allowNewUser,
        @Column("maintenance") boolean maintenance,
        @Column("maintenance_text") String maintenanceText,
        @Column("ai_system_prompt") String aiSystemPrompt,
        @CreatedDate @Column("created_at") LocalDateTime createdAt,
        @LastModifiedDate @Column("modified_at") LocalDateTime modifiedAt,
        @CreatedBy @Column("created_by") String createdBy,
        @LastModifiedBy @Column("modified_by") String modifiedBy) {
}
