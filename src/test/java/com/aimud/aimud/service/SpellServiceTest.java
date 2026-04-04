package com.aimud.aimud.service;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Mobile;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

        spellService.registerSpells();

        assertThat(spellService.getSpell("missile")).isInstanceOf(TestMagicMissile.class);
        assertThat(spellService.getAllSpells()).hasSize(1);
        assertThat(spellService.getSpellMap()).containsOnlyKeys("missile");
        verifyNoInteractions(skillService);
    }

    @Test
    void syncSpellSkills_registersSpellSkillsAfterStartupWithoutFailingApplication() {
        Map<String, Spell> beans = new LinkedHashMap<>();
        TestMagicMissile magicMissile = new TestMagicMissile();
        DuplicateSpell duplicateSpell = new DuplicateSpell();
        beans.put("magicMissile", magicMissile);
        beans.put("duplicateSpell", duplicateSpell);

        when(applicationContext.getBeansOfType(Spell.class)).thenReturn(beans);
        doAnswer(invocation -> {
            String skillName = invocation.getArgument(0);
            Long skillId = invocation.getArgument(1);
            if ("Spell: Duplicate Spell".equals(skillName) && skillId == 2001L) {
                throw new IllegalStateException("skills registry unavailable");
            }
            return null;
        })
                .when(skillService)
                .createSkill(anyString(), anyLong());

        spellService.registerSpells();

        assertThatCode(() -> spellService.syncSpellSkills())
                .doesNotThrowAnyException();

        verify(skillService).createSkill("Spell: Magic Missile", 1000L);
        verify(skillService).createSkill("Spell: Duplicate Spell", 2001L);
    }

    @MagicSpell(name = "missile")
    private static class TestMagicMissile extends Spell {
        private TestMagicMissile() {
            super(null, null, null, null, null);
        }

        @Override
        public String getSpellName() {
            return "Magic Missile";
        }

        @Override
        public Long getSpellId() {
            return 1000L;
        }

        @Override
        public Integer getSpellLevel() {
            return 1;
        }

        @Override
        public String getDescription() {
            return "A basic missile of force.";
        }

        @Override
        public boolean cast(Mobile mobile, Spell spell, Mobile target) {
            return true;
        }
    }

    private static class PlainSpell extends Spell {
        private PlainSpell() {
            super(null, null, null, null, null);
        }

        @Override
        public String getSpellName() {
            return "No Annotation";
        }

        @Override
        public Long getSpellId() {
            return 9999L;
        }

        @Override
        public Integer getSpellLevel() {
            return 1;
        }

        @Override
        public String getDescription() {
            return "No annotation.";
        }

        @Override
        public boolean cast(Mobile mobile, Spell spell, Mobile target) {
            return false;
        }
    }

    @MagicSpell(name = "duplicate")
    private static class DuplicateSpell extends Spell {
        private DuplicateSpell() {
            super(null, null, null, null, null);
        }

        @Override
        public String getSpellName() {
            return "Duplicate Spell";
        }

        @Override
        public Long getSpellId() {
            return 2001L;
        }

        @Override
        public Integer getSpellLevel() {
            return 1;
        }

        @Override
        public String getDescription() {
            return "A spell used for testing.";
        }

        @Override
        public boolean cast(Mobile mobile, Spell spell, Mobile target) {
            return true;
        }
    }
}

