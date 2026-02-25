package com.aimud.aimud;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("app_user")
public record AppUser(
        @Id Long id,
        @Column("email") String email,
        @Column("password_hash") String passwordHash,
        @Column("roles") String roles) {
}
