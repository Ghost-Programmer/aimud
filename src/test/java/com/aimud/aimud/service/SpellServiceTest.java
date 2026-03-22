package com.aimud.aimud.service;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.spells.Spell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpellServiceTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SkillService skillService;

    private SpellService spellService;

    @BeforeEach
    void setUp() {
        spellService = new SpellService(applicationContext, skillService);
    }

    @Test
    void registerSpells_registersAnnotatedSpellsByMagicSpellName() {
        Map<String, Spell> beans = new LinkedHashMap<>();
        beans.put("magicMissile", new TestMagicMissile());
        beans.put("plainSpell", new PlainSpell());

        when(applicationContext.getBeansOfType(Spell.class)).thenReturn(beans);
        when(skillService.existsSkill("Spell: Magic Missile")).thenReturn(false);

        spellService.registerSpells();

        assertThat(spellService.getSpell("missile")).isInstanceOf(TestMagicMissile.class);
        assertThat(spellService.getAllSpells()).hasSize(1);
        assertThat(spellService.getSpellMap()).containsOnlyKeys("missile");

        verify(skillService).existsSkill("Spell: Magic Missile");
        verify(skillService).createSkill("Spell: Magic Missile", 1000L);
    }

    @Test
    void registerSpells_duplicateMagicSpellName_overwritesPreviousEntry() {
        Map<String, Spell> beans = new LinkedHashMap<>();
        beans.put("first", new DuplicateSpellOne());
        beans.put("second", new DuplicateSpellTwo());

        when(applicationContext.getBeansOfType(Spell.class)).thenReturn(beans);
        when(skillService.existsSkill("Spell: First Duplicate")).thenReturn(true);
        when(skillService.existsSkill("Spell: Second Duplicate")).thenReturn(true);

        spellService.registerSpells();

        assertThat(spellService.getAllSpells()).hasSize(1);
        assertThat(spellService.getSpell("duplicate")).isInstanceOf(DuplicateSpellTwo.class);
        verify(skillService, never()).createSkill("Spell: First Duplicate", 2001L);
        verify(skillService, never()).createSkill("Spell: Second Duplicate", 2002L);
    }

    @MagicSpell(name = "missile")
    private static class TestMagicMissile implements Spell {
        @Override
        public String getSpellName() {
            return "Magic Missile";
        }

        @Override
        public Long getSpellId() {
            return 1000L;
        }
    }

    private static class PlainSpell implements Spell {
        @Override
        public String getSpellName() {
            return "No Annotation";
        }

        @Override
        public Long getSpellId() {
            return 9999L;
        }
    }

    @MagicSpell(name = "duplicate")
    private static class DuplicateSpellOne implements Spell {
        @Override
        public String getSpellName() {
            return "First Duplicate";
        }

        @Override
        public Long getSpellId() {
            return 2001L;
        }
    }

    @MagicSpell(name = "duplicate")
    private static class DuplicateSpellTwo implements Spell {
        @Override
        public String getSpellName() {
            return "Second Duplicate";
        }

        @Override
        public Long getSpellId() {
            return 2002L;
        }
    }
}

