package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.prayers.Prayer;
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
public class PrayerService {

    private final ApplicationContext context;
    private final Map<String, Prayer> prayerMap = new LinkedHashMap<>();
    private final SkillService skillService;

    public PrayerService(ApplicationContext context, SkillService skillService) {
        this.context = context;
        this.skillService = skillService;
    }

    @PostConstruct
    public void registerPrayers() {
        prayerMap.clear();
        Map<String, Prayer> beans = context.getBeansOfType(Prayer.class);

        for (Prayer bean : beans.values()) {
            // Resolve against target class so Spring proxies still expose annotations.
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            DivinePrayer annotation = AnnotationUtils.findAnnotation(targetClass, DivinePrayer.class);
            if (annotation != null) {
                String prayerName = annotation.name();
                if (prayerName == null || prayerName.isBlank()) {
                    log.warn("Skipping prayer {} because @DivinePrayer name is blank", targetClass.getName());
                    continue;
                }

                Prayer previous = prayerMap.put(prayerName, bean);
                if (previous != null) {
                    log.warn("Duplicate @DivinePrayer name '{}' found on {}. Replacing {}", prayerName,
                            targetClass.getName(), previous.getClass().getName());
                }

                log.info("Registered prayer: {}", prayerName);
            }
        }

        log.info("Registered {} prayers: {}", prayerMap.size(),
                String.join(", ", prayerMap.keySet()));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncPrayerSkills() {
        for (Prayer prayer : prayerMap.values()) {
            try {
                skillService.createSkill(prayer.getPrayerSkillName(), prayer.getPrayerId());
            } catch (IllegalStateException ex) {
                log.warn("Unable to synchronize prayer skill '{}' during startup. Continuing without failing application initialization.",
                        prayer.getPrayerSkillName(), ex);
            }
        }
    }

    public Prayer getPrayer(String name) {
        return prayerMap.get(name);
    }

    public List<Prayer> getAllPrayers() {
        return new ArrayList<>(prayerMap.values());
    }

    public Map<String, Prayer> getPrayerMap() {
        return new LinkedHashMap<>(prayerMap);
    }
}
