package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.event.NpcInteractionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for handling Retrieval-Augmented Generation (RAG)
 * operations.
 * It listens for interaction events and stores them in the vector database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final VectorStore vectorStore;
    private final ConfigService configService;

    /**
     * Listens for {@link NpcInteractionEvent} and stores the interaction data
     * into the vector database.
     *
     * @param event the npc interaction event
     */
    @EventListener
    public void handleNpcInteraction(NpcInteractionEvent event) {
        log.info("Received NPC interaction event for NPC ID {} and PC ID {} with interaction: {}", event.npcId(),
                event.pcId(), event.interaction());

        configService.getServerSettings()
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .subscribe(settings -> {
                    try {
                        Map<String, Object> metadata = Map.of(
                                "npcId", event.npcId(),
                                "pcId", event.pcId(),
                                "mudMonth", settings.mudMonth(),
                                "mudDay", settings.mudDay(),
                                "mudYear", settings.mudYear(),
                                "mudHour", settings.mudHour());

                        Document document = new Document(event.interaction(), metadata);
                        vectorStore.add(List.of(document));

                        log.debug("Successfully stored interaction in vector store.");
                    } catch (Exception e) {
                        log.error("Failed to store NPC interaction in vector database", e);
                    }
                });
    }
}
