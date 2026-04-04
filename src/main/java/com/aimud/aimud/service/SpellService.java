package com.aimud.aimud.service;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.spells.Spell;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SpellService {

    private final ApplicationContext context;
    private final Map<String, Spell> spellMap = new LinkedHashMap<>();
    private final SkillService skillService;

    public SpellService(ApplicationContext context, SkillService skillService) {
        this.context = context;
        this.skillService = skillService;
    }

    @PostConstruct
    public void registerSpells() {
        spellMap.clear();
        Map<String, Spell> beans = context.getBeansOfType(Spell.class);

        for (Spell bean : beans.values()) {
            // Resolve against target class so Spring proxies still expose annotations.
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            MagicSpell annotation = AnnotationUtils.findAnnotation(targetClass, MagicSpell.class);
            if (annotation != null) {
                String spellName = annotation.name();
                if (spellName == null || spellName.isBlank()) {
                    log.warn("Skipping spell {} because @MagicSpell name is blank", targetClass.getName());
                    continue;
                }

                Spell previous = spellMap.put(spellName, bean);
                if (previous != null) {
                    log.warn("Duplicate @MagicSpell name '{}' found on {}. Replacing {}", spellName,
                            targetClass.getName(), previous.getClass().getName());
                }

                log.info("Registered spell: {}", spellName);
            }
        }

        log.info("Registered {} spells: {}", spellMap.size(),
                String.join(", ", spellMap.keySet()));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncSpellSkills() {
        for (Spell spell : spellMap.values()) {
            try {
                skillService.createSkill(spell.getSpellSkillName(), spell.getSpellId());
            } catch (IllegalStateException ex) {
                log.warn("Unable to synchronize spell skill '{}' during startup. Continuing without failing application initialization.",
                        spell.getSpellSkillName(), ex);
            }
        }
    }

    public Spell getSpell(String name) {
        return spellMap.get(name);
    }

    public List<Spell> getAllSpells() {
        return new ArrayList<>(spellMap.values());
    }

    public Map<String, Spell> getSpellMap() {
        return new LinkedHashMap<>(spellMap);
    }
}

