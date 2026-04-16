package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Faction;
import io.nadia.ai.aimud.service.FactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactionControllerTest {

    @Mock
    private FactionService factionService;

    @Test
    void getAllFactions_DelegatesToService() {
        FactionController controller = new FactionController(factionService);
        Faction faction = new Faction();
        faction.setId(1L);
        when(factionService.findAllFactions()).thenReturn(Flux.just(faction));

        StepVerifier.create(controller.getAllFactions())
                .expectNext(faction)
                .verifyComplete();

        verify(factionService).findAllFactions();
    }

    @Test
    void updateMobileRatings_DelegatesToService() {
        FactionController controller = new FactionController(factionService);
        Map<Long, Integer> ratings = Map.of(1L, 80);
        when(factionService.updateMobileRatings(7L, ratings)).thenReturn(Mono.empty());

        StepVerifier.create(controller.updateMobileRatings(7L, ratings))
                .verifyComplete();

        verify(factionService).updateMobileRatings(7L, ratings);
    }
}

