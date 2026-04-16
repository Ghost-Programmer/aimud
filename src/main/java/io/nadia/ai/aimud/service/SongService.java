package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.songs.Song;
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
public class SongService {

    private final ApplicationContext context;
    private final Map<String, Song> songMap = new LinkedHashMap<>();
    private final SkillService skillService;

    /**
     * Constructs a new SongService.
     *
     * @param context      the application context for bean resolution
     * @param skillService the skill service to register song skills
     */
    public SongService(ApplicationContext context, SkillService skillService) {
        this.context = context;
        this.skillService = skillService;
    }

    /**
     * Scans the application context for components implementing {@link Song} and registers them.
     */
    @PostConstruct
    public void registerSongs() {
        songMap.clear();
        Map<String, Song> beans = context.getBeansOfType(Song.class);

        for (Song bean : beans.values()) {
            // Resolve against target class so Spring proxies still expose annotations.
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            BardSong annotation = AnnotationUtils.findAnnotation(targetClass, BardSong.class);
            if (annotation != null) {
                String songName = annotation.name();
                if (songName == null || songName.isBlank()) {
                    log.warn("Skipping song {} because @BardSong name is blank", targetClass.getName());
                    continue;
                }

                Song previous = songMap.put(songName, bean);
                if (previous != null) {
                    log.warn("Duplicate @BardSong name '{}' found on {}. Replacing {}", songName,
                            targetClass.getName(), previous.getClass().getName());
                }

                log.info("Registered song: {}", songName);
            }
        }

        log.info("Registered {} songs: {}", songMap.size(),
                String.join(", ", songMap.keySet()));
    }

    /**
     * Synchronizes registered songs with the skill database upon application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncSongSkills() {
        for (Song song : songMap.values()) {
            try {
                skillService.createSkill(song.getSongSkillName(), song.getSongId());
            } catch (IllegalStateException ex) {
                log.warn("Unable to synchronize song skill '{}' during startup. Continuing without failing application initialization.",
                        song.getSongSkillName(), ex);
            }
        }
    }

    /**
     * Retrieves a specialized song implementation by its registered name.
     *
     * @param name the name of the song
     * @return the {@link Song} instance, or null if not found
     */
    public Song getSong(String name) {
        return songMap.get(name);
    }

    /**
     * Retrieves a list of all registered song implementations.
     *
     * @return a list of all songs
     */
    public List<Song> getAllSongs() {
        return new ArrayList<>(songMap.values());
    }

    /**
     * Retrieves the backing map of all registered songs.
     *
     * @return a map of song names to their implementations
     */
    public Map<String, Song> getSongMap() {
        return new LinkedHashMap<>(songMap);
    }
}
