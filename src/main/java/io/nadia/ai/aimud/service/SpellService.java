package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.spells.Spell;
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

    /**
     * Constructs a new SpellService.
     *
     * @param context      the application context for bean resolution
     * @param skillService the skill service to register spell skills
     */
    public SpellService(ApplicationContext context, SkillService skillService) {
        this.context = context;
        this.skillService = skillService;
    }

    /**
     * Scans the application context for components implementing {@link Spell} and registers them.
     */
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

    /**
     * Synchronizes registered spells with the skill database upon application startup.
     */
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

    /**
     * Retrieves a specialized spell implementation by its registered name.
     *
     * @param name the name of the spell
     * @return the {@link Spell} instance, or null if not found
     */
    public Spell getSpell(String name) {
        return spellMap.get(name);
    }

    /**
     * Retrieves a list of all registered spell implementations.
     *
     * @return a list of all spells
     */
    public List<Spell> getAllSpells() {
        return new ArrayList<>(spellMap.values());
    }

    /**
     * Retrieves the backing map of all registered spells.
     *
     * @return a map of spell names to their implementations
     */
    public Map<String, Spell> getSpellMap() {
        return new LinkedHashMap<>(spellMap);
    }
}

