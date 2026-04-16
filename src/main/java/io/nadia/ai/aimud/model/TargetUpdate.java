package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetUpdate {
    private Mobile character;
    private Mobile target;
}
