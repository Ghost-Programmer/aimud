package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Skill;
import io.nadia.ai.aimud.repository.SkillRegistryRepository;
import io.nadia.ai.aimud.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock private SkillRepository skillRepository;
    @Mock private SkillRegistryRepository skillRegistryRepository;
    @Mock private DatabaseClient databaseClient;

    private SkillService skillService;

    @BeforeEach
    void setUp() {
        skillService = new SkillService(skillRepository, skillRegistryRepository, databaseClient);
    }

    // --- getSkillRank ---

    @Test
    void getSkillRank_ReturnsRankForKnownSkill() {
        Mobile mobile = new Mobile();
        Skill skill = Skill.builder().name("Slash").rank(5).build();
        mobile.setSkills(new ArrayList<>(List.of(skill)));
        assertThat(skillService.getSkillRank(mobile, "Slash")).isEqualTo(5);
    }

    @Test
    void getSkillRank_ReturnsZeroForUnknownSkill() {
        Mobile mobile = new Mobile();
        mobile.setSkills(new ArrayList<>());
        assertThat(skillService.getSkillRank(mobile, "Stab")).isZero();
    }

    @Test
    void getSkillRank_IsCaseInsensitive() {
        Mobile mobile = new Mobile();
        Skill skill = Skill.builder().name("SLASH").rank(3).build();
        mobile.setSkills(new ArrayList<>(List.of(skill)));
        assertThat(skillService.getSkillRank(mobile, "slash")).isEqualTo(3);
    }

    @Test
    void getSkillRank_NullSkillListReturnsZero() {
        Mobile mobile = new Mobile();
        mobile.setSkills(null);
        assertThat(skillService.getSkillRank(mobile, "Bash")).isZero();
    }

    // --- getDiminishingReturnMultiplier ---

    @Test
    void getDiminishingReturnMultiplier_Below75Returns1() {
        assertThat(skillService.getDiminishingReturnMultiplier(0)).isEqualTo(1.0);
        assertThat(skillService.getDiminishingReturnMultiplier(74)).isEqualTo(1.0);
    }

    @Test
    void getDiminishingReturnMultiplier_Between75And89Returns0_5() {
        assertThat(skillService.getDiminishingReturnMultiplier(75)).isEqualTo(0.5);
        assertThat(skillService.getDiminishingReturnMultiplier(89)).isEqualTo(0.5);
    }

    @Test
    void getDiminishingReturnMultiplier_90AndAboveReturns0_1() {
        assertThat(skillService.getDiminishingReturnMultiplier(90)).isEqualTo(0.1);
        assertThat(skillService.getDiminishingReturnMultiplier(100)).isEqualTo(0.1);
    }

    // --- shouldSkillImprove ---

    @Test
    void shouldSkillImprove_CappedSkillNeverImproves() {
        // 100 skill level should never increase
        for (int i = 0; i < 100; i++) {
            assertThat(skillService.shouldSkillImprove(100, 5.0f, 5.0f, true)).isFalse();
        }
    }

    @Test
    void shouldSkillImprove_TrivialChallengeNeverImproves() {
        // targetCr much lower than playerLevel + MIN_CR_DELTA (-5)
        for (int i = 0; i < 100; i++) {
            assertThat(skillService.shouldSkillImprove(50, 10.0f, 1.0f, true)).isFalse();
        }
    }

    @Test
    void shouldSkillImprove_ZeroSkillAgainstHighCrImprovesFrequently() {
        int successes = 0;
        for (int i = 0; i < 1000; i++) {
            if (skillService.shouldSkillImprove(0, 1.0f, 20.0f, true)) successes++;
        }
        // probability ≈ 15% → expect between 50 and 350 successes out of 1000
        assertThat(successes).isBetween(50, 350);
    }

    @Test
    void shouldSkillImprove_FailureGivesHigherImprovementChanceThanSuccess() {
        int successImprovements = 0;
        int failureImprovements = 0;
        for (int i = 0; i < 10000; i++) {
            if (skillService.shouldSkillImprove(10, 2.0f, 5.0f, true)) successImprovements++;
            if (skillService.shouldSkillImprove(10, 2.0f, 5.0f, false)) failureImprovements++;
        }
        assertThat(failureImprovements).isGreaterThan(successImprovements);
    }

    // --- addSkill ---

    @Test
    void addSkill_ReturnsExistingSkillIfPresent() {
        Mobile mobile = new Mobile();
        Skill existing = Skill.builder().characterId(1L).name("Slash").rank(3).build();
        mobile.setSkills(new ArrayList<>(List.of(existing)));
        mobile.setId(1L);

        StepVerifier.create(skillService.addSkill(mobile, "Slash"))
                .expectNext(existing)
                .verifyComplete();

        verify(skillRepository, never()).save(any());
    }

    @Test
    void addSkill_CreatesNewSkillIfNotPresent() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        mobile.setSkills(new ArrayList<>());

        Skill saved = Skill.builder().characterId(1L).name("Bash").rank(1).build();
        when(skillRepository.save(any(Skill.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(skillService.addSkill(mobile, "Bash"))
                .assertNext(s -> {
                    assertThat(s.getName()).isEqualTo("Bash");
                    assertThat(s.getRank()).isEqualTo(1);
                })
                .verifyComplete();

        verify(skillRepository).save(any(Skill.class));
    }

    // --- checkSkill ---

    @Test
    void checkSkill_EmptySkillListReturnsEmpty() {
        Mobile mobile = new Mobile();
        mobile.setSkills(null);

        StepVerifier.create(skillService.checkSkill(mobile, "Slash", 5.0f, true))
                .verifyComplete();
    }

    @Test
    void checkSkill_SkillNotFoundReturnsEmpty() {
        Mobile mobile = new Mobile();
        mobile.setSkills(new ArrayList<>());

        StepVerifier.create(skillService.checkSkill(mobile, "Slash", 5.0f, true))
                .verifyComplete();
    }
}

