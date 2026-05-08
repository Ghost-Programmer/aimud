package io.nadia.ai.aimud.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Table("game_logs")
public class GameLog {
    @Id
    private Long id;
    private Long mobileId;
    private boolean isWorldLog;
    private String message;
    private LocalDateTime createdAt;
}
