package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.repository.FactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactionServiceTest {

    @Mock private FactionRepository factionRepository;
    @Mock private DatabaseClient databaseClient;

    private FactionService factionService;

    @BeforeEach
    void setUp() {
        factionService = new FactionService(factionRepository, databaseClient);
    }

    // --- getFactionRating defaults ---

    @Test
    void getFactionRating_NullTargetFactionIdReturnsNeutral() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        mobile.setFactionId(10L);

        StepVerifier.create(factionService.getFactionRating(mobile, null))
                .expectNext(50)
                .verifyComplete();
    }

    @Test
    void getFactionRating_NullMobileFactionIdReturnsNeutral() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        mobile.setFactionId(null);

        StepVerifier.create(factionService.getFactionRating(mobile, 5L))
                .expectNext(50)
                .verifyComplete();
    }

    @Test
    void getFactionRating_SameFactionReturns100() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        mobile.setFactionId(7L);

        StepVerifier.create(factionService.getFactionRating(mobile, 7L))
                .expectNext(100)
                .verifyComplete();
    }

    @Test
    void getFactionRating_CachesResultAfterFirstLookup() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        mobile.setFactionId(10L);

        // Use RETURNS_DEEP_STUBS so the full call chain db.sql(...).bind(...).bind(...).map(...).first().defaultIfEmpty() works
        DatabaseClient.GenericExecuteSpec spec = mock(DatabaseClient.GenericExecuteSpec.class, RETURNS_DEEP_STUBS);
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(eq("mobileId"), eq(1L))).thenReturn(spec);
        when(spec.bind(eq("factionId"), eq(20L))).thenReturn(spec);
        when(spec.map(any(java.util.function.BiFunction.class)).first().defaultIfEmpty(50))
                .thenReturn(reactor.core.publisher.Mono.just(75));

        // First call hits DB
        StepVerifier.create(factionService.getFactionRating(mobile, 20L))
                .expectNext(75)
                .verifyComplete();

        // Second call should use cache — no additional DB interactions
        StepVerifier.create(factionService.getFactionRating(mobile, 20L))
                .expectNext(75)
                .verifyComplete();

        // DB was only queried once
        verify(databaseClient, times(1)).sql(anyString());
    }

    // --- getFactionRatingSync defaults ---

    @Test
    void getFactionRatingSync_NullTargetFactionReturnsNeutral() {
        Mobile mobile = new Mobile();
        mobile.setFactionId(1L);
        assertThat(factionService.getFactionRatingSync(mobile, null)).isEqualTo(50);
    }

    @Test
    void getFactionRatingSync_NullMobileFactionReturnsNeutral() {
        Mobile mobile = new Mobile();
        mobile.setFactionId(null);
        assertThat(factionService.getFactionRatingSync(mobile, 5L)).isEqualTo(50);
    }

    @Test
    void getFactionRatingSync_SameFactionReturns100() {
        Mobile mobile = new Mobile();
        mobile.setFactionId(3L);
        assertThat(factionService.getFactionRatingSync(mobile, 3L)).isEqualTo(100);
    }

    // --- modifyFactionRating ---

    @Test
    void modifyFactionRating_NullMobileIdCompletesEmpty() {
        Mobile mobile = new Mobile();
        mobile.setId(null);
        StepVerifier.create(factionService.modifyFactionRating(mobile, 5L, 10))
                .verifyComplete();
    }

    @Test
    void modifyFactionRating_NullTargetFactionCompletesEmpty() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        StepVerifier.create(factionService.modifyFactionRating(mobile, null, 10))
                .verifyComplete();
    }

    // --- handleKillPenalty ---

    @Test
    void handleKillPenalty_NullVictimFactionCompletesEmpty() {
        Mobile attacker = new Mobile();
        attacker.setId(1L);
        attacker.setFactionId(10L);
        Mobile victim = new Mobile();
        victim.setFactionId(null);

        StepVerifier.create(factionService.handleKillPenalty(attacker, victim))
                .verifyComplete();
    }
}

