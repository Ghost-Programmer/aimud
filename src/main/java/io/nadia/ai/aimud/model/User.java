package io.nadia.ai.aimud.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Fundamental authentication record mapping web portal and game client credentials.
 * Used exclusively for external identity validation. Does not represent in-game active state.
 */
@Setter
@Getter
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String role;
    private boolean locked;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("modified_at")
    private LocalDateTime modifiedAt;

    @CreatedBy
    @Column("created_by")
    private String createdBy;

    @LastModifiedBy
    @Column("modified_by")
    private String modifiedBy;

    /**
     * No-args instantiation mapping utilized directly by core data hydration pipelines.
     */
    public User() {
    }

    /**
     * Base initialization signature handling rudimentary credential pairings before role assertions.
     *
     * @param username String-based login alias
     * @param password raw passkey logic (must be digested downstream)
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
