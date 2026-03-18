package com.aimud.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetUpdate {
    private Character character;
    private Mobile target;
}
