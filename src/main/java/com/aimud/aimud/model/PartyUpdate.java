package com.aimud.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PartyUpdate {
    private Long characterId;
    private Long leaderId;
    private List<PartyMemberInfo> members;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PartyMemberInfo {
        private Long id;
        private String name;
        private int currentHp;
        private int maxHp;
        private int currentMana;
        private int maxMana;
    }
}
